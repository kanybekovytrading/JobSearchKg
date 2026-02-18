package job.search.kg.service.user;

import job.search.kg.dto.response.payment.CheckRecipientResponse;
import job.search.kg.dto.response.payment.MakePaymentResponse;
import job.search.kg.dto.response.user.CreatePaymentResponse;
import job.search.kg.entity.*;
import job.search.kg.exceptions.InsufficientBalanceException;
import job.search.kg.exceptions.ResourceNotFoundException;
import job.search.kg.payment.FinikConfig;
import job.search.kg.payment.FinikPaymentsGatewayService;
import job.search.kg.payment.PaymentService;
import job.search.kg.payment.WithdrawalService;
import job.search.kg.repo.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BoostService {

    // Стоимость буста
    private static final int BOOST_COST_SOMS = 20;      // 20 сом за день
    private static final int BOOST_COST_POINTS = 400;   // 200 баллов за день
    private static final int BOOST_DURATION_HOURS = 24; // 24 часа
    private final VacancyRepository vacancyRepository;
    private final ResumeRepository resumeRepository;
    private final VacancyBoostRepository vacancyBoostRepository;
    private final ResumeBoostRepository resumeBoostRepository;
    private final UserRepository userRepository;
    private final BotPointsService pointsService;
    private final PaymentService paymentService;

    /**
     * Поднять вакансию за баллы
     */
    @Transactional
    public VacancyBoost boostVacancyWithPoints(Long telegramId, Long vacancyId) {
        User user = userRepository.findByTelegramId(telegramId)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));

        Vacancy vacancy = vacancyRepository.findById(vacancyId)
                .orElseThrow(() -> new ResourceNotFoundException("Вакансия не найдена"));

        // Проверяем владельца
        if (!vacancy.getUser().getId().equals(user.getId())) {
            throw new IllegalStateException("Вы можете поднимать только свои вакансии");
        }

        // Проверяем баланс
        if (!pointsService.hasEnoughPoints(telegramId, BOOST_COST_POINTS)) {
            throw new InsufficientBalanceException("Недостаточно баллов для поднятия");
        }

        // Списываем баллы
        pointsService.deductPoints(
                telegramId,
                BOOST_COST_POINTS,
                PointsTransaction.TransactionType.BOOST,
                "Поднятие вакансии: " + vacancy.getTitle()
        );

        // Деактивируем старые бусты
        deactivateOldVacancyBoosts(vacancyId);

        // Создаем новый буст
        VacancyBoost boost = VacancyBoost.builder()
                .vacancy(vacancy)
                .user(user)
                .boostDate(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusHours(BOOST_DURATION_HOURS))
                .paymentType(VacancyBoost.PaymentType.POINTS)
                .amountPaid(BOOST_COST_POINTS)
                .isActive(true)
                .build();

        return vacancyBoostRepository.save(boost);
    }

    /**
     * Поднять вакансию за деньги
     */
    @Transactional
    public CreatePaymentResponse boostVacancyWithMoney(
            Long telegramId,
            Long vacancyId
    ) throws Exception {
        User user = userRepository.findByTelegramId(telegramId)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));

        Vacancy vacancy = vacancyRepository.findById(vacancyId)
                .orElseThrow(() -> new ResourceNotFoundException("Вакансия не найдена"));

        if (!vacancy.getUser().getId().equals(user.getId())) {
            throw new IllegalStateException("Вы можете поднимать только свои вакансии");
        }
        CreatePaymentResponse createPaymentResponse = paymentService.createPayment(telegramId, BigDecimal.valueOf(BOOST_COST_SOMS), "вакансии", "string");
        // Создаем буст
        VacancyBoost boost = VacancyBoost.builder()
                .vacancy(vacancy)
                .user(user)
                .boostDate(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusHours(BOOST_DURATION_HOURS))
                .paymentType(VacancyBoost.PaymentType.MONEY)
                .amountPaid(BOOST_COST_SOMS)
                .isActive(false)
                .paymentId(createPaymentResponse.getPaymentId())
                .build();

        vacancyBoostRepository.save(boost);
        return createPaymentResponse;
    }

    /**
     * Поднять резюме за баллы
     */
    @Transactional
    public ResumeBoost boostResumeWithPoints(Long telegramId, Long resumeId) {
        User user = userRepository.findByTelegramId(telegramId)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));

        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found"));

        if (!resume.getUser().getId().equals(user.getId())) {
            throw new IllegalStateException("Вы можете поднимать только свои резюме");
        }

        if (!pointsService.hasEnoughPoints(telegramId, BOOST_COST_POINTS)) {
            throw new InsufficientBalanceException("Недостаточно баллов для поднятия");
        }

        pointsService.deductPoints(
                telegramId,
                BOOST_COST_POINTS,
                PointsTransaction.TransactionType.BOOST,
                "Поднятие резюме: " + resume.getName()
        );

        deactivateOldResumeBoosts(resumeId);

        ResumeBoost boost = ResumeBoost.builder()
                .resume(resume)
                .user(user)
                .boostDate(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusHours(BOOST_DURATION_HOURS))
                .paymentType(ResumeBoost.PaymentType.POINTS)
                .amountPaid(BOOST_COST_POINTS)
                .isActive(true)
                .build();

        return resumeBoostRepository.save(boost);
    }

    /**
     * Поднять резюме за деньги
     */
    @Transactional
    public CreatePaymentResponse boostResumeWithMoney(
            Long telegramId,
            Long resumeId
    ) throws Exception {
        User user = userRepository.findByTelegramId(telegramId)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));

        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found"));

        if (!resume.getUser().getId().equals(user.getId())) {
            throw new IllegalStateException("Вы можете поднимать только свои резюме");
        }

        CreatePaymentResponse createPaymentResponse = paymentService.createPayment(telegramId, BigDecimal.valueOf(BOOST_COST_SOMS), "резюме", "string");


        deactivateOldResumeBoosts(resumeId);

        ResumeBoost boost = ResumeBoost.builder()
                .resume(resume)
                .user(user)
                .boostDate(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusHours(BOOST_DURATION_HOURS))
                .paymentType(ResumeBoost.PaymentType.MONEY)
                .amountPaid(BOOST_COST_SOMS)
                .paymentId(createPaymentResponse.getPaymentId())
                .isActive(false)
                .build();

        resumeBoostRepository.save(boost);
        return createPaymentResponse;
    }

    /**
     * Проверить, есть ли активный буст
     */
    @Transactional(readOnly = true)
    public boolean hasActiveVacancyBoost(Long vacancyId) {
        return vacancyBoostRepository.existsByVacancyIdAndIsActiveTrueAndExpiresAtAfter(
                vacancyId,
                LocalDateTime.now()
        );
    }

    @Transactional(readOnly = true)
    public boolean hasActiveResumeBoost(Long resumeId) {
        return resumeBoostRepository.existsByResumeIdAndIsActiveTrueAndExpiresAtAfter(
                resumeId,
                LocalDateTime.now()
        );
    }

    public void deactivateOldVacancyBoosts(Long vacancyId) {
        List<VacancyBoost> oldBoosts = vacancyBoostRepository
                .findByVacancyIdAndIsActiveTrue(vacancyId);
        oldBoosts.forEach(boost -> boost.setIsActive(false));
        vacancyBoostRepository.saveAll(oldBoosts);
    }

    public void deactivateOldResumeBoosts(Long resumeId) {
        List<ResumeBoost> oldBoosts = resumeBoostRepository
                .findByResumeIdAndIsActiveTrue(resumeId);
        oldBoosts.forEach(boost -> boost.setIsActive(false));
        resumeBoostRepository.saveAll(oldBoosts);
    }

    private String formatPhoneNumber(String phone) {
        String cleaned = phone.replaceAll("[^0-9]", "");
        if (cleaned.startsWith("996")) {
            return "+" + cleaned;
        }
        if (cleaned.startsWith("0")) {
            return "+996" + cleaned.substring(1);
        }
        if (cleaned.length() == 9) {
            return "+996" + cleaned;
        }
        return "+" + cleaned;
    }
}

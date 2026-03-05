package job.search.kg.payment;

import job.search.kg.dto.response.admin.PaymentAnalyticsResponse;
import job.search.kg.dto.response.user.CreatePaymentResponse;
import job.search.kg.dto.response.user.PaymentResponse;
import job.search.kg.entity.Payment;
import job.search.kg.entity.Subscription;
import job.search.kg.entity.User;
import job.search.kg.repo.PaymentRepository;
import job.search.kg.repo.SubscriptionRepository;
import job.search.kg.repo.UserRepository;
import job.search.kg.service.user.BotSubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {


    private static final BigDecimal PAYMENT_GATEWAY_FEE_PERCENTAGE = new BigDecimal("0.02"); // 2%
    private static final BigDecimal PARTNER_SHARE_PERCENTAGE = new BigDecimal("0.25");        // 25%
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final FinikPaymentService finikPaymentService;
    private final BotSubscriptionService botSubscriptionService;
    private final SubscriptionRepository subscriptionRepository;
    private final FinikConfig finikConfig;

    /**
     * ✅ АНАЛИТИКА ПЛАТЕЖЕЙ С ФИЛЬТРОМ ПО ДАТАМ
     */
    @Transactional(readOnly = true)
    public PaymentAnalyticsResponse getPaymentAnalytics(
            LocalDateTime startDate,
            LocalDateTime endDate
    ) {
        // Если даты не указаны, берем все время
        if (startDate == null) {
            startDate = LocalDateTime.of(2000, 1, 1, 0, 0);
        }
        if (endDate == null) {
            endDate = LocalDateTime.now();
        }

        // Активные подписки (на текущий момент)
        Long activeSubscriptions = subscriptionRepository.countByIsActive(true);

        // Всего куплено подписок за период
        Long totalSubscriptions = subscriptionRepository
                .countByCreatedAtBetween(startDate, endDate);

        // Все платежи за период
        List<Payment> allPayments = paymentRepository
                .findByCreatedAtBetween(startDate, endDate);

        // Успешные платежи
        BigDecimal totalPaid = allPayments.stream()
                .filter(p -> p.getStatus() == Payment.PaymentStatus.SUCCESS)
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Ожидающие
        BigDecimal totalPending = allPayments.stream()
                .filter(p -> p.getStatus() == Payment.PaymentStatus.PENDING)
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Неудачные
        BigDecimal totalFailed = allPayments.stream()
                .filter(p -> p.getStatus() == Payment.PaymentStatus.FAILED)
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Расчет комиссий и долей
        BigDecimal totalRevenue = totalPaid;

        // 1. Комиссия платежной системы 2%
        BigDecimal paymentGatewayFee = totalRevenue
                .multiply(PAYMENT_GATEWAY_FEE_PERCENTAGE)
                .setScale(2, RoundingMode.HALF_UP);

        // 2. Сумма после комиссии платежки
        BigDecimal afterGatewayFee = totalRevenue
                .subtract(paymentGatewayFee)
                .setScale(2, RoundingMode.HALF_UP);

        // 3. Доля партнера 25% (от суммы после платежки)
        BigDecimal partnerShare = afterGatewayFee
                .multiply(PARTNER_SHARE_PERCENTAGE)
                .setScale(2, RoundingMode.HALF_UP);

        // 4. Чистая прибыль (остаток)
        BigDecimal netProfit = afterGatewayFee
                .subtract(partnerShare)
                .setScale(2, RoundingMode.HALF_UP);

        return PaymentAnalyticsResponse.builder()
                .activeSubscriptionsCount(activeSubscriptions)
                .totalSubscriptionsPurchased(totalSubscriptions)
                .totalRevenue(totalRevenue)
                .paymentGatewayFee(paymentGatewayFee)
                .afterGatewayFee(afterGatewayFee)
                .partnerShare(partnerShare)
                .netProfit(netProfit)
                .totalPaid(totalPaid)
                .totalPending(totalPending)
                .totalFailed(totalFailed)
                .build();
    }

    /**
     * Создание платежа для теста
     */
    @Transactional
    public CreatePaymentResponse createPayment(
            Long telegramId,
            Subscription.PlanType planType,
            String redirectURL
    ) throws Exception {

        // 1. Валидация
        if (telegramId == null || planType == null) {
            throw new IllegalArgumentException("telegramId and planType are required");
        }

        // 2. Проверяем пользователя
        User user = userRepository.findByTelegramId(telegramId)
                .orElseThrow(() -> new RuntimeException("User not found"));


        boolean hasActiveSubs = botSubscriptionService.hasActiveSubscription(telegramId);

        if (hasActiveSubs) {
            throw new RuntimeException("Already have active subscription. Wait its expiration and try again");
        }
        // 5. Создаем запись в БД СНАЧАЛА
        Payment payment = new Payment();
        payment.setUser(user);
        payment.setPaymentId(UUID.randomUUID().toString());
        payment.setAmount(BigDecimal.valueOf(getSubscriptionCost(planType)));
        payment.setStatus(Payment.PaymentStatus.PENDING);
        payment.setPlanType(planType);
        payment.setCreatedAt(LocalDateTime.now());
        payment = paymentRepository.save(payment);

        log.info("Payment record created: id={}, paymentId={}, telegramId={}, planType={}",
                payment.getId(), payment.getPaymentId(), telegramId, planType);

        try {
            String description = "Оплата тарифа: " + getSubscriptionTrans(planType);
            String paymentUrl = finikPaymentService.createPayment(
                    UUID.fromString(payment.getPaymentId()),
                    payment.getAmount(),
                    description,
                    redirectURL
            );

            // 7. Сохраняем URL
            payment.setPaymentUrl(paymentUrl);
            payment = paymentRepository.save(payment);

            log.info("Payment URL received: paymentId={}, url={}",
                    payment.getPaymentId(), paymentUrl);

            // 8. Возвращаем ответ
            return new CreatePaymentResponse(
                    payment.getPaymentId(),
                    paymentUrl,
                    Payment.PaymentStatus.PENDING.name()
            );

        } catch (Exception e) {
            // При ошибке помечаем платеж как FAILED
            payment.setStatus(Payment.PaymentStatus.FAILED);
            paymentRepository.save(payment);

            log.error("Failed to create payment in Finik: paymentId={}",
                    payment.getPaymentId(), e);
            throw e;
        }
    }

    @Transactional
    public CreatePaymentResponse createPayment(
            Long telegramId,
            BigDecimal amount,
            String type,
            String redirectURL
    ) throws Exception {

        // 1. Валидация
        if (telegramId == null) {
            throw new IllegalArgumentException("telegramId and planType are required");
        }

        // 2. Проверяем пользователя
        User user = userRepository.findByTelegramId(telegramId)
                .orElseThrow(() -> new RuntimeException("User not found"));


        boolean hasActiveSubs = botSubscriptionService.hasActiveSubscription(telegramId);

        if (hasActiveSubs) {
            throw new RuntimeException("Already have active subscription. Wait its expiration and try again");
        }
        // 5. Создаем запись в БД СНАЧАЛА
        Payment payment = new Payment();
        payment.setUser(user);
        payment.setPaymentId(UUID.randomUUID().toString());
        payment.setAmount(amount);
        payment.setStatus(Payment.PaymentStatus.PENDING);
        payment.setCreatedAt(LocalDateTime.now());
        payment = paymentRepository.save(payment);

        log.info("Payment record created: id={}, paymentId={}, telegramId={}",
                payment.getId(), payment.getPaymentId(), telegramId);

        try {
            String description = "Оплата за поднятие показа " + type;
            String paymentUrl = finikPaymentService.createPayment(
                    UUID.fromString(payment.getPaymentId()),
                    payment.getAmount(),
                    description,
                    redirectURL
            );

            // 7. Сохраняем URL
            payment.setPaymentUrl(paymentUrl);
            payment = paymentRepository.save(payment);

            log.info("Payment URL received: paymentId={}, url={}",
                    payment.getPaymentId(), paymentUrl);

            // 8. Возвращаем ответ
            return new CreatePaymentResponse(
                    payment.getPaymentId(),
                    paymentUrl,
                    Payment.PaymentStatus.PENDING.name()
            );


        } catch (Exception e) {
            // При ошибке помечаем платеж как FAILED
            payment.setStatus(Payment.PaymentStatus.FAILED);
            paymentRepository.save(payment);

            log.error("Failed to create payment in Finik: paymentId={}",
                    payment.getPaymentId(), e);
            throw e;
        }
    }

    /**
     * Получение платежа по ID
     */
    public PaymentResponse getPayment(UUID paymentId) {
        Payment payment = paymentRepository.findByPaymentId(String.valueOf(paymentId))
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        return mapToResponse(payment);
    }

    /**
     * Получение истории платежей пользователя
     */
    public List<PaymentResponse> getUserPayments(Long telegramId) {
        return paymentRepository.findByUserTelegramIdOrderByCreatedAtDesc(telegramId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private PaymentResponse mapToResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .paymentId(UUID.fromString(payment.getPaymentId()))
                .paymentUrl(payment.getPaymentUrl())
                .amount(payment.getAmount())
                .status(payment.getStatus())
                .transactionId(payment.getTransactionId())
                .createdAt(payment.getCreatedAt())
                .paidAt(payment.getCompletedAt())
                .build();
    }

    private int getSubscriptionCost(Subscription.PlanType planType) {
        return switch (planType) {
            case THREE_DAYS -> 169;
            case ONE_WEEK -> 149;      // 150 сом = 1500 баллов
            case ONE_MONTH -> 290;     // 500 сом = 5000 баллов
            case THREE_MONTHS -> 490; // 1200 сом = 12000 баллов
        };
    }

    private String getSubscriptionTrans(Subscription.PlanType planType) {
        return switch (planType) {
            case THREE_DAYS -> "3-дневный";
            case ONE_WEEK -> "Недельный";      // 150 сом = 1500 баллов
            case ONE_MONTH -> "1-месячный";     // 500 сом = 5000 баллов
            case THREE_MONTHS -> "3-месячный"; // 1200 сом = 12000 баллов
        };
    }
}

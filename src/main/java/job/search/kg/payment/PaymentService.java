package job.search.kg.payment;

import job.search.kg.dto.response.user.CreatePaymentResponse;
import job.search.kg.dto.response.user.PaymentResponse;
import job.search.kg.entity.Payment;
import job.search.kg.entity.Subscription;
import job.search.kg.entity.User;
import job.search.kg.repo.PaymentRepository;
import job.search.kg.repo.UserRepository;
import job.search.kg.service.user.BotSubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {


    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final FinikPaymentService finikPaymentService;
    private final BotSubscriptionService botSubscriptionService;
    private final FinikConfig finikConfig;

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


       boolean hasActiveSubs =  botSubscriptionService.hasActiveSubscription(telegramId);

       if(hasActiveSubs){
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
            String description = "Оплата теста: " + planType;
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
            case ONE_WEEK -> 150;      // 150 сом = 1500 баллов
            case ONE_MONTH -> 500;     // 500 сом = 5000 баллов
            case THREE_MONTHS -> 1200; // 1200 сом = 12000 баллов
        };
    }
}

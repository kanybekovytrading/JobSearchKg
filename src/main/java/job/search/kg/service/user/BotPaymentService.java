package job.search.kg.service.user;

import job.search.kg.client.FinikApiClient;
import job.search.kg.dto.request.user.CreatePaymentRequest;
import job.search.kg.entity.Payment;
import job.search.kg.entity.Subscription;
import job.search.kg.entity.User;
import job.search.kg.exceptions.InvalidPaymentException;
import job.search.kg.telegram.TelegramService;
import job.search.kg.exceptions.ResourceNotFoundException;
import job.search.kg.repo.PaymentRepository;
import job.search.kg.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;


@Service
@RequiredArgsConstructor
public class BotPaymentService {

    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final BotSubscriptionService subscriptionService;
//    private final FinikApiClient finikApiClient;
    private final TelegramService telegramService;

//    @Transactional
//    public PaymentResponse createPayment(Long telegramId, CreatePaymentRequest request) {
//        User user = userRepository.findByTelegramId(telegramId)
//                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
//
//        BigDecimal amount = getAmountForPlan(request.getPlanType());
//
//        // Создание платежа в Финик
//        Map<String, Object> finikResponse = finikApiClient.createPayment(
//                amount,
//                "Подписка Work KG - " + getPlanName(request.getPlanType()),
//                telegramId,
//                request.getPlanType()
//        );

        // Сохранение в БД
//        Payment payment = new Payment();
//        payment.setUser(user);
//        payment.setPaymentId((String) finikResponse.get("payment_id"));
//        payment.setAmount(amount);
//        payment.setPlanType(request.getPlanType());
//        payment.setStatus(Payment.PaymentStatus.PENDING);
//        payment.setPaymentUrl((String) finikResponse.get("payment_url"));
//
//        paymentRepository.save(payment);
//
//        PaymentResponse response = new PaymentResponse();
//        response.setPaymentId(payment.getPaymentId());
//        response.setPaymentUrl(payment.getPaymentUrl());
//        response.setAmount(payment.getAmount());
//        response.setStatus(payment.getStatus());
//
//        return response;
//    }

    @Transactional
    public void processWebhook(Map<String, Object> webhookData) {
        String paymentId = (String) webhookData.get("payment_id");
        String status = (String) webhookData.get("status");

        Payment payment = paymentRepository.findByPaymentId(paymentId)
                .orElseThrow(() -> new InvalidPaymentException("Payment not found"));

        if ("success".equals(status)) {
            payment.setStatus(Payment.PaymentStatus.SUCCESS);
            payment.setCompletedAt(LocalDateTime.now());
            paymentRepository.save(payment);

            // Активация подписки
            Subscription subscription = subscriptionService.createSubscription(
                    payment.getUser().getTelegramId(),
                    payment.getPlanType(),
                    paymentId
            );

            // Уведомление пользователю
            telegramService.sendMessage(
                    payment.getUser().getTelegramId(),
                    String.format(
                            "✅ Подписка успешно активирована!\n\n" +
                                    "Тариф: %s\n" +
                                    "Действует до: %s\n\n" +
                                    "Теперь вам доступны все функции бота!",
                            getPlanName(payment.getPlanType()),
                            subscription.getEndDate().toLocalDate()
                    )
            );

        } else if ("failed".equals(status)) {
            payment.setStatus(Payment.PaymentStatus.FAILED);
            paymentRepository.save(payment);

            // Уведомление об ошибке
            telegramService.sendMessage(
                    payment.getUser().getTelegramId(),
                    "❌ Оплата не прошла. Попробуйте снова или свяжитесь с поддержкой."
            );
        }
    }

    private BigDecimal getAmountForPlan(Subscription.PlanType planType) {
        return switch (planType) {
            case ONE_WEEK -> new BigDecimal("150.00");
            case ONE_MONTH -> new BigDecimal("500.00");
            case THREE_MONTHS -> new BigDecimal("1200.00");
        };
    }

    private String getPlanName(Subscription.PlanType planType) {
        return switch (planType) {
            case ONE_WEEK -> "1 неделя";
            case ONE_MONTH -> "1 месяц";
            case THREE_MONTHS -> "3 месяца";
        };
    }
}

package job.search.kg.payment;

import job.search.kg.dto.response.user.WebhookData;
import job.search.kg.entity.Payment;
import job.search.kg.entity.ResumeBoost;
import job.search.kg.entity.Subscription;
import job.search.kg.entity.User;
import job.search.kg.entity.VacancyBoost;
import job.search.kg.repo.PaymentRepository;
import job.search.kg.repo.ResumeBoostRepository;
import job.search.kg.repo.VacancyBoostRepository;
import job.search.kg.service.user.BoostService;
import job.search.kg.service.user.BotSubscriptionService;
import job.search.kg.telegram.TelegramService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FinikWebhookService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private final PaymentRepository paymentRepository;
    private final BotSubscriptionService botSubscriptionService;
    private final BoostService boostService;
    private final VacancyBoostRepository vacancyBoostRepository;
    private final ResumeBoostRepository resumeBoostRepository;
    private final TelegramService telegramService;

    @Transactional
    public void processWebhook(WebhookData webhook) {
        log.info("webhook {}", webhook.toString());
        // Ищем платеж по transactionId
        Optional<Payment> existingPayment = paymentRepository
                .findByTransactionId(webhook.getTransactionId());

        if (existingPayment.isPresent()) {
            log.warn("Webhook already processed: transactionId={}", webhook.getTransactionId());
            return; // Идемпотентность
        }

        // Извлекаем PaymentId: пробуем fields["PaymentId"], fields["paymentId"], затем id
        String paymentIdStr = extractPaymentId(webhook);
        if (paymentIdStr == null) {
            log.error("Cannot extract paymentId from webhook: id={}, transactionId={}, fields={}",
                    webhook.getId(), webhook.getTransactionId(), webhook.getFields());
            return;
        }

        Optional<Payment> paymentOpt = paymentRepository.findByPaymentId(paymentIdStr);
        if (paymentOpt.isEmpty()) {
            log.error("Payment not found for paymentId={} (webhook id={}, transactionId={})",
                    paymentIdStr, webhook.getId(), webhook.getTransactionId());
            return;
        }
        Payment payment = paymentOpt.get();

        // Обновляем статус
        payment.setTransactionId(webhook.getTransactionId());
        payment.setReceiptNumber(webhook.getReceiptNumber());

        if ("SUCCEEDED".equalsIgnoreCase(webhook.getStatus())) {
            payment.setStatus(Payment.PaymentStatus.SUCCESS);
            payment.setCompletedAt(LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(webhook.getTransactionDate()),
                    ZoneId.systemDefault()
            ));

            log.info("Payment succeeded: paymentId={}", payment.getPaymentId());

            if (payment.getPlanType() != null) {
                try {
                    botSubscriptionService.createSubscription(payment.getUser().getTelegramId(), payment.getPlanType(), paymentIdStr);
                    sendSubscriptionNotification(payment.getUser(), payment.getPlanType());
                } catch (Exception e) {
                    log.error("Failed to activate subscription for paymentId={}, user={}: {}",
                            paymentIdStr, payment.getUser().getTelegramId(), e.getMessage(), e);
                    sendSupportNotification(payment.getUser());
                }
            } else {
                VacancyBoost vacancyBoost = vacancyBoostRepository.findByPaymentId(payment.getPaymentId()).orElse(null);
                if (vacancyBoost != null) {
                    boostService.deactivateOldVacancyBoosts(vacancyBoost.getId());
                    vacancyBoost.setIsActive(true);
                    vacancyBoostRepository.save(vacancyBoost);
                } else {
                    ResumeBoost resumeBoost = resumeBoostRepository.findByPaymentId(payment.getPaymentId()).orElse(null);
                    if (resumeBoost != null) {
                        boostService.deactivateOldResumeBoosts(resumeBoost.getId());
                        resumeBoost.setIsActive(true);
                        resumeBoostRepository.save(resumeBoost);
                    }
                }
            }
        } else {
            payment.setStatus(Payment.PaymentStatus.PENDING);
            log.warn("Payment failed: paymentId={}", payment.getPaymentId());
        }

        paymentRepository.save(payment);
    }

    private void sendSupportNotification(User user) {
        try {
            String msg = switch (user.getLanguage()) {
                case RU -> "⚠️ <b>Ваш платёж получен, но подписка не активировалась.</b>\n\n" +
                           "Пожалуйста, свяжитесь с поддержкой:\n" +
                           "📞 <b>+996 707 720 086</b>";
                case KY -> "⚠️ <b>Төлөмүңүз кабыл алынды, бирок жазылуу активдешкен жок.</b>\n\n" +
                           "Колдоо кызматына кайрылыңыз:\n" +
                           "📞 <b>+996 707 720 086</b>";
                case EN -> "⚠️ <b>Your payment was received but subscription wasn't activated.</b>\n\n" +
                           "Please contact support:\n" +
                           "📞 <b>+996 707 720 086</b>";
            };
            telegramService.sendMessage(user.getTelegramId(), msg);
        } catch (Exception e) {
            log.error("Failed to send support notification to user {}", user.getTelegramId(), e);
        }
    }

    private void sendSubscriptionNotification(User user, Subscription.PlanType planType) {
        try {
            LocalDateTime endDate = LocalDateTime.now().plusDays(switch (planType) {
                case THREE_DAYS -> 3;
                case ONE_WEEK -> 7;
                case ONE_MONTH -> 30;
                case THREE_MONTHS -> 90;
            });
            String planName = switch (user.getLanguage()) {
                case RU -> switch (planType) {
                    case THREE_DAYS -> "3 дня";
                    case ONE_WEEK -> "1 неделя";
                    case ONE_MONTH -> "1 месяц";
                    case THREE_MONTHS -> "3 месяца";
                };
                case KY -> switch (planType) {
                    case THREE_DAYS -> "3 күн";
                    case ONE_WEEK -> "1 апта";
                    case ONE_MONTH -> "1 ай";
                    case THREE_MONTHS -> "3 ай";
                };
                case EN -> switch (planType) {
                    case THREE_DAYS -> "3 days";
                    case ONE_WEEK -> "1 week";
                    case ONE_MONTH -> "1 month";
                    case THREE_MONTHS -> "3 months";
                };
            };
            String msg = switch (user.getLanguage()) {
                case RU -> String.format(
                        "✅ <b>Подписка активирована!</b>\n\n" +
                        "📦 Тариф: <b>%s</b>\n" +
                        "📅 Действует до: <b>%s</b>\n\n" +
                        "Приятного использования! 🎉",
                        planName, endDate.format(DATE_FMT));
                case KY -> String.format(
                        "✅ <b>Жазылуу активдештирилди!</b>\n\n" +
                        "📦 Тариф: <b>%s</b>\n" +
                        "📅 Аяктаган күнү: <b>%s</b>\n\n" +
                        "Ырахат алыңыз! 🎉",
                        planName, endDate.format(DATE_FMT));
                case EN -> String.format(
                        "✅ <b>Subscription activated!</b>\n\n" +
                        "📦 Plan: <b>%s</b>\n" +
                        "📅 Valid until: <b>%s</b>\n\n" +
                        "Enjoy! 🎉",
                        planName, endDate.format(DATE_FMT));
            };
            telegramService.sendMessage(user.getTelegramId(), msg);
        } catch (Exception e) {
            log.error("Failed to send subscription notification to user {}", user.getTelegramId(), e);
        }
    }

    private String extractPaymentId(WebhookData webhook) {
        if (webhook.getFields() != null) {
            Object val = webhook.getFields().get("PaymentId");
            if (val instanceof String s && !s.isBlank()) return s;
            val = webhook.getFields().get("paymentId");
            if (val instanceof String s && !s.isBlank()) return s;
        }
        if (webhook.getId() != null && !webhook.getId().isBlank()) {
            return webhook.getId();
        }
        return null;
    }
}

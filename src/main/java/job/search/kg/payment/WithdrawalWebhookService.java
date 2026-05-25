package job.search.kg.payment;

import job.search.kg.dto.request.payment.FinikWebhookPayload;
import job.search.kg.entity.PointsTransaction;
import job.search.kg.entity.Withdrawal;
import job.search.kg.entity.User;
import job.search.kg.repo.PointsTransactionRepository;
import job.search.kg.repo.UserRepository;
import job.search.kg.repo.WithdrawalRepository;
import job.search.kg.telegram.TelegramService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.math.BigDecimal;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class WithdrawalWebhookService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    private final WithdrawalRepository withdrawalRepository;
    private final UserRepository userRepository;
    private final PointsTransactionRepository pointsTransactionRepository;
    private final TelegramService telegramService;

    /**
     * Обработка webhook от Finik
     */
    @Transactional
    public void processPaymentStatusUpdate(FinikWebhookPayload payload) {

        String transactionId = payload.getTransactionId();
        String status = payload.getStatus();

        log.info("Processing webhook: transactionId={}, status={}", transactionId, status);

        // 1. Находим withdrawal по transactionId
        Optional<Withdrawal> optionalWithdrawal = withdrawalRepository
                .findByTransactionId(transactionId);

        if (optionalWithdrawal.isEmpty()) {
            log.warn("Withdrawal not found for transactionId: {}", transactionId);
            return;
        }

        Withdrawal withdrawal = optionalWithdrawal.get();

        // 2. Проверка идемпотентности - если уже обработан, пропускаем
        if (withdrawal.getStatus() == Withdrawal.WithdrawalStatus.SUCCEEDED ||
                withdrawal.getStatus() == Withdrawal.WithdrawalStatus.FAILED) {
            log.info("Withdrawal {} already in final state: {}",
                    withdrawal.getId(), withdrawal.getStatus());
            return;
        }

        // 3. Обновляем Finik transaction ID если еще не установлен
        if (withdrawal.getFinikTransactionId() == null && payload.getId() != null) {
            withdrawal.setFinikTransactionId(payload.getId());
        }

        // 4. Обрабатываем статус
        switch (status) {
            case "SUCCEEDED" -> handleSucceededPayment(withdrawal, payload);
            case "FAILED" -> handleFailedPayment(withdrawal, payload);
            case "PROCESSING" -> handleProcessingPayment(withdrawal);
            default -> log.warn("Unknown payment status: {} for withdrawal {}",
                    status, withdrawal.getId());
        }

        withdrawalRepository.save(withdrawal);
    }

    /**
     * Обработка успешного платежа
     */
    private void handleSucceededPayment(Withdrawal withdrawal, FinikWebhookPayload payload) {
        withdrawal.setStatus(Withdrawal.WithdrawalStatus.SUCCEEDED);

        if (payload.getTransactionDate() != null) {
            withdrawal.setCompletedAt(LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(payload.getTransactionDate()),
                    ZoneId.systemDefault()
            ));
        } else {
            withdrawal.setCompletedAt(LocalDateTime.now());
        }

        log.info("Withdrawal {} marked as SUCCEEDED", withdrawal.getId());

        // Отправляем уведомление пользователю
        sendSuccessNotification(withdrawal);
    }

    /**
     * Обработка неудачного платежа
     */
    private void handleFailedPayment(Withdrawal withdrawal, FinikWebhookPayload payload) {
        withdrawal.setStatus(Withdrawal.WithdrawalStatus.FAILED);
        withdrawal.setErrorMessage("Payment failed on provider side");

        log.warn("Withdrawal {} marked as FAILED", withdrawal.getId());

        // Возвращаем баллы пользователю
        if (withdrawal.getPointsTransactionId() != null) {
            try {
                PointsTransaction original = pointsTransactionRepository
                        .findById(withdrawal.getPointsTransactionId())
                        .orElse(null);
                if (original != null) {
                    int refundAmount = Math.abs(original.getAmount());
                    User user = withdrawal.getUser();
                    user.setBalance(user.getBalance() + refundAmount);
                    userRepository.save(user);

                    PointsTransaction refund = new PointsTransaction();
                    refund.setUser(user);
                    refund.setAmount(refundAmount);
                    refund.setType(PointsTransaction.TransactionType.REFUND);
                    refund.setDescription("Возврат баллов после неудачного вывода (webhook)");
                    pointsTransactionRepository.save(refund);

                    log.info("Refunded {} points to user {} for failed withdrawal {}",
                            refundAmount, user.getTelegramId(), withdrawal.getId());
                }
            } catch (Exception e) {
                log.error("Failed to refund points for withdrawal {}", withdrawal.getId(), e);
            }
        }

        // Отправляем уведомление пользователю
        sendFailureNotification(withdrawal);
    }

    /**
     * Обработка платежа в процессе
     */
    private void handleProcessingPayment(Withdrawal withdrawal) {
        withdrawal.setStatus(Withdrawal.WithdrawalStatus.PROCESSING);
        log.debug("Withdrawal {} still in PROCESSING", withdrawal.getId());
    }

    /**
     * Отправка уведомления об успехе
     */
    private void sendSuccessNotification(Withdrawal withdrawal) {
        try {
            User user = withdrawal.getUser();
            if (user == null || user.getTelegramId() == null) {
                return;
            }

            String message = getSuccessMessage(
                    user.getLanguage(),
                    withdrawal.getAmount(),
                    withdrawal.getCompletedAt()
            );

            telegramService.sendMessage(user.getTelegramId(), message);
            log.info("Sent success notification for withdrawal {}", withdrawal.getId());

        } catch (Exception e) {
            log.error("Failed to send success notification: {}", e.getMessage());
        }
    }

    /**
     * Отправка уведомления о неудаче
     */
    private void sendFailureNotification(Withdrawal withdrawal) {
        try {
            User user = withdrawal.getUser();
            if (user == null || user.getTelegramId() == null) {
                return;
            }

            String message = getFailureMessage(
                    user.getLanguage(),
                    withdrawal.getAmount()
            );

            telegramService.sendMessage(user.getTelegramId(), message);
            log.info("Sent failure notification for withdrawal {}", withdrawal.getId());

        } catch (Exception e) {
            log.error("Failed to send failure notification: {}", e.getMessage());
        }
    }

    private String getSuccessMessage(User.Language language, BigDecimal amount, LocalDateTime completedAt) {
        String dateTime = completedAt != null
                ? completedAt.format(DATE_TIME_FORMATTER)
                : LocalDateTime.now().format(DATE_TIME_FORMATTER);

        return switch (language) {
            case RU -> String.format(
                    "✅ <b>Выплата успешно выполнена!</b>\n\n" +
                            "💰 Сумма: <b>%.2f сом</b>\n" +
                            "📅 Дата: %s\n\n" +
                            "Средства поступят на ваш счет в течение нескольких минут.",
                    amount, dateTime
            );
            case KY -> String.format(
                    "✅ <b>Төлөм ийгиликтүү аткарылды!</b>\n\n" +
                            "💰 Сумма: <b>%.2f сом</b>\n" +
                            "📅 Дата: %s\n\n" +
                            "Акча бир нече мүнөттүн ичинде эсебиңизге түшөт.",
                    amount, dateTime
            );
            case EN -> String.format(
                    "✅ <b>Withdrawal successful!</b>\n\n" +
                            "💰 Amount: <b>%.2f som</b>\n" +
                            "📅 Date: %s\n\n" +
                            "Funds will arrive in your account within a few minutes.",
                    amount, dateTime
            );
        };
    }

    private String getFailureMessage(User.Language language, BigDecimal amount) {
        return switch (language) {
            case RU -> String.format(
                    "❌ <b>Выплата не выполнена</b>\n\n" +
                            "💰 Сумма: <b>%.2f сом</b>\n\n" +
                            "Произошла ошибка при обработке выплаты. " +
                            "Средства возвращены на ваш баланс.\n\n" +
                            "Пожалуйста, попробуйте снова или свяжитесь с поддержкой.",
                    amount
            );
            case KY -> String.format(
                    "❌ <b>Төлөм аткарылган жок</b>\n\n" +
                            "💰 Сумма: <b>%.2f сом</b>\n\n" +
                            "Төлөмдү иштетүүдө ката кетти. " +
                            "Акча балансыңызга кайтарылды.\n\n" +
                            "Кайра аракет кылыңыз же колдоо кызматына кайрылыңыз.",
                    amount
            );
            case EN -> String.format(
                    "❌ <b>Withdrawal failed</b>\n\n" +
                            "💰 Amount: <b>%.2f som</b>\n\n" +
                            "An error occurred while processing the withdrawal. " +
                            "Funds have been returned to your balance.\n\n" +
                            "Please try again or contact support.",
                    amount
            );
        };
    }
}

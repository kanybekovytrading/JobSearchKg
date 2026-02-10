package job.search.kg.scheduler;

import job.search.kg.dto.response.payment.PaymentStatusResponse;
import job.search.kg.entity.Withdrawal;
import job.search.kg.entity.User;
import job.search.kg.payment.FinikPaymentsGatewayService;
import job.search.kg.repo.WithdrawalRepository;
import job.search.kg.telegram.TelegramService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Планировщик для проверки статуса выводов в статусе PROCESSING
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WithdrawalStatusScheduler {

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    private final WithdrawalRepository withdrawalRepository;
    private final FinikPaymentsGatewayService paymentsGatewayService;
    private final TelegramService telegramService;

    /**
     * Каждые 30 секунд проверяем статус выводов в PROCESSING
     * По документации, 99% платежей завершаются в течение 30 секунд
     */
    @Scheduled(fixedRate = 30000) // 30 секунд
    public void checkPendingWithdrawals() {

        List<Withdrawal> processingWithdrawals = withdrawalRepository
                .findByStatus(Withdrawal.WithdrawalStatus.PROCESSING);

        if (processingWithdrawals.isEmpty()) {
            return;
        }

        log.info("Checking status of {} processing withdrawals", processingWithdrawals.size());

        for (Withdrawal withdrawal : processingWithdrawals) {
            try {
                // Проверяем статус только если есть Finik transaction ID
                if (withdrawal.getFinikTransactionId() == null) {
                    log.warn("Withdrawal {} has no Finik transaction ID, skipping",
                            withdrawal.getId());
                    continue;
                }

                PaymentStatusResponse statusResponse = paymentsGatewayService
                        .checkPaymentStatus(withdrawal.getFinikTransactionId());

                // Проверяем HTTP код ответа
                if (statusResponse.getStatusCode() != 200) {
                    // Ошибка на уровне API (403, 404, 500 и т.д.)
                    log.error("Payment status check returned error code: withdrawalId={}, statusCode={}, error={}",
                            withdrawal.getId(),
                            statusResponse.getStatusCode(),
                            statusResponse.getErrorMessage());

                    // Для критических ошибок (403, 404) можем пометить как FAILED
                    if (statusResponse.getStatusCode() == 403 || statusResponse.getStatusCode() == 404) {
                        withdrawal.setStatus(Withdrawal.WithdrawalStatus.FAILED);
                        log.info("SCHEDULER FAILED PAYMENT {}", statusResponse.getErrorMessage());
                        withdrawal.setErrorMessage(statusResponse.getErrorMessage());
                        withdrawalRepository.save(withdrawal);
                        sendWithdrawalFailedNotification(withdrawal);
                    }
                    // Для временных ошибок (500, 502, 503) оставляем в PROCESSING
                    continue;
                }

                // Обрабатываем статус платежа (когда statusCode = 200)
                String status = statusResponse.getStatus();

                if ("SUCCEEDED".equals(status)) {
                    withdrawal.setStatus(Withdrawal.WithdrawalStatus.SUCCEEDED);
                    if (statusResponse.getTransactionDate() != null) {
                        withdrawal.setCompletedAt(LocalDateTime.ofInstant(
                                Instant.ofEpochMilli(statusResponse.getTransactionDate()),
                                ZoneId.systemDefault()
                        ));
                    }
                    withdrawalRepository.save(withdrawal);
                    log.info("Withdrawal {} succeeded", withdrawal.getId());
                    sendWithdrawalSuccessNotification(withdrawal);

                } else if ("FAILED".equals(status)) {
                    withdrawal.setStatus(Withdrawal.WithdrawalStatus.FAILED);
                    withdrawal.setErrorMessage("Payment failed on provider side");
                    withdrawalRepository.save(withdrawal);
                    log.warn("Withdrawal {} failed", withdrawal.getId());
                    sendWithdrawalFailedNotification(withdrawal);

                } else if ("PROCESSING".equals(status)) {
                    // Все еще в обработке, ничего не делаем
                    log.debug("Withdrawal {} still processing", withdrawal.getId());

                } else {
                    // Неизвестный статус
                    log.warn("Withdrawal {} has unknown status: {}",
                            withdrawal.getId(), status);
                }

            } catch (Exception e) {
                log.error("Error checking withdrawal status: id={}",
                        withdrawal.getId(), e);
            }
        }
    }

    /**
     * Каждый час проверяем старые PROCESSING выводы
     * Если вывод в PROCESSING более 1 часа, помечаем как FAILED
     */
    @Scheduled(fixedRate = 3600000) // 1 час
    public void markOldWithdrawalsAsFailed() {

        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);

        List<Withdrawal> processingWithdrawals = withdrawalRepository
                .findByStatus(Withdrawal.WithdrawalStatus.PROCESSING);

        int marked = 0;
        for (Withdrawal withdrawal : processingWithdrawals) {
            if (withdrawal.getCreatedAt().isBefore(oneHourAgo)) {
                withdrawal.setStatus(Withdrawal.WithdrawalStatus.FAILED);
                withdrawal.setErrorMessage("Transaction timeout - exceeded 1 hour");
                withdrawalRepository.save(withdrawal);
                marked++;

                log.warn("Marked old withdrawal as failed: id={}, createdAt={}",
                        withdrawal.getId(), withdrawal.getCreatedAt());

                sendWithdrawalTimeoutNotification(withdrawal);
            }
        }

        if (marked > 0) {
            log.info("Marked {} old withdrawals as failed", marked);
        }
    }

    /**
     * Отправка уведомления об успешной выплате
     */
    private void sendWithdrawalSuccessNotification(Withdrawal withdrawal) {
        try {
            User user = withdrawal.getUser();
            if (user == null || user.getTelegramId() == null) {
                return;
            }

            String message = getWithdrawalSuccessMessage(
                    user.getLanguage(),
                    withdrawal.getAmount(),
                    withdrawal.getCompletedAt()
            );

            telegramService.sendMessage(user.getTelegramId(), message);
            log.info("Sent success notification for withdrawal {}", withdrawal.getId());
        } catch (Exception e) {
            log.error("Failed to send success notification for withdrawal {}: {}",
                    withdrawal.getId(), e.getMessage());
        }
    }

    /**
     * Отправка уведомления о неудачной выплате
     */
    private void sendWithdrawalFailedNotification(Withdrawal withdrawal) {
        try {
            User user = withdrawal.getUser();
            if (user == null || user.getTelegramId() == null) {
                return;
            }

            String message = getWithdrawalFailedMessage(
                    user.getLanguage(),
                    withdrawal.getAmount()
            );

            telegramService.sendMessage(user.getTelegramId(), message);
            log.info("Sent failure notification for withdrawal {}", withdrawal.getId());
        } catch (Exception e) {
            log.error("Failed to send failure notification for withdrawal {}: {}",
                    withdrawal.getId(), e.getMessage());
        }
    }

    /**
     * Отправка уведомления о таймауте выплаты
     */
    private void sendWithdrawalTimeoutNotification(Withdrawal withdrawal) {
        try {
            User user = withdrawal.getUser();
            if (user == null || user.getTelegramId() == null) {
                return;
            }

            String message = getWithdrawalTimeoutMessage(
                    user.getLanguage(),
                    withdrawal.getAmount()
            );
            telegramService.sendMessage(user.getTelegramId(), message);
            log.info("Sent timeout notification for withdrawal {}", withdrawal.getId());
        } catch (Exception e) {
            log.error("Failed to send timeout notification for withdrawal {}: {}",
                    withdrawal.getId(), e.getMessage());
        }
    }

    /**
     * Формирование сообщения об успешной выплате
     */
    private String getWithdrawalSuccessMessage(
            User.Language language,
            BigDecimal amount,
            LocalDateTime completedAt
    ) {
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

    /**
     * Формирование сообщения о неудачной выплате
     */
    private String getWithdrawalFailedMessage(
            User.Language language,
            BigDecimal amount
    ) {
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
                            "Төлөмдү иштетүүдөката кетти. " +
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

    /**
     * Формирование сообщения о таймауте выплаты
     */
    private String getWithdrawalTimeoutMessage(
            User.Language language,
            BigDecimal amount
    ) {
        return switch (language) {
            case RU -> String.format(
                    "⏱ <b>Время ожидания выплаты истекло</b>\n\n" +
                            "💰 Сумма: <b>%.2f сом</b>\n\n" +
                            "Выплата не была завершена в течение часа.\n\n" +
                            "Пожалуйста, попробуйте снова или свяжитесь с поддержкой.",
                    amount
            );
            case KY -> String.format(
                    "⏱ <b>Төлөмдү күтүү убакыты бүттү</b>\n\n" +
                            "💰 Сумма: <b>%.2f сом</b>\n\n" +
                            "Төлөм бир саат ичинде аяктаган жок. " +
                            "Кайра аракет кылыңыз же колдоо кызматына кайрылыңыз.",
                    amount
            );
            case EN -> String.format(
                    "⏱ <b>Withdrawal timeout</b>\n\n" +
                            "💰 Amount: <b>%.2f som</b>\n\n" +
                            "The withdrawal was not completed within an hour. " +
                            "Please try again or contact support.",
                    amount
            );
        };
    }
}
package job.search.kg.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "withdrawals")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Withdrawal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Пользователь, который делает вывод
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Уникальный ID транзакции для предотвращения дубликатов
     */
    @Column(nullable = false, unique = true)
    private String transactionId;

    /**
     * ID транзакции в системе Finik (получаем в ответе)
     */
    @Column(name = "finik_transaction_id")
    private String finikTransactionId;

    /**
     * ID услуги (например: "averspay")
     */
    @Column(nullable = false)
    private String serviceId;

    /**
     * Название услуги (например: "Finik", "O!", "Mega")
     */
    private String serviceName;
    /**
     * Номер телефона получателя
     */
    @Column(nullable = false)
    private String recipientPhone;

    /**
     * Имя получателя (если доступно)
     */
    private String recipientName;

    /**
     * Сумма вывода
     */
    @Column(nullable = false)
    private BigDecimal amount;

    /**
     * ID счета Finik, откуда списываются средства
     */
    @Column(nullable = false)
    private String accountId;

    /**
     * Статус вывода
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WithdrawalStatus status;

    /**
     * Дата создания запроса
     */
    @Column(nullable = false)
    private LocalDateTime createdAt;

    /**
     * Дата завершения транзакции
     */
    private LocalDateTime completedAt;

    private Long pointsTransactionId;

    /**
     * Сообщение об ошибке (если есть)
     */
    @Column(length = 1000)
    private String errorMessage;

    public enum WithdrawalStatus {
        PENDING,      // Ожидает обработки
        PROCESSING,   // В процессе
        SUCCEEDED,    // Успешно
        FAILED,       // Ошибка
        CANCELED      // Отменен
    }
}

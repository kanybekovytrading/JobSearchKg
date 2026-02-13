package job.search.kg.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "free_access_tracking",
        indexes = {
                @Index(name = "idx_telegram_search_date",
                        columnList = "telegram_id, search_key, access_date")
        })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FreeAccessTracking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "telegram_id", nullable = false)
    private Long telegramId;

    @Column(name = "search_key", nullable = false, length = 100)
    private String searchKey; // Формат: RESUME_C1_S2_CAT3_SUB4 или VACANCY_C1_S2_CAT3_SUB4

    @Column(name = "entity_id", nullable = false)
    private Long entityId; // ID резюме или вакансии

    @Column(name = "access_date", nullable = false)
    private LocalDate accessDate;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
package job.search.kg.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "vacancy_responses", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"vacancy_id", "resume_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VacancyResponse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vacancy_id", nullable = false)
    private Vacancy vacancy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false)
    private Resume resume;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "applicant_id", nullable = false)
    private User applicant; // Кто откликнулся

    @Column(name = "message", columnDefinition = "TEXT")
    private String message; // Сопроводительное письмо

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ResponseStatus status = ResponseStatus.PENDING;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "viewed_at")
    private LocalDateTime viewedAt;

    public enum ResponseStatus {
        PENDING,    // Ожидает рассмотрения
        VIEWED,     // Просмотрено
        ACCEPTED,   // Принято
        REJECTED    // Отклонено
    }
}
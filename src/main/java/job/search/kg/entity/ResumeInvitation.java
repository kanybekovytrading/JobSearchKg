package job.search.kg.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "resume_invitations", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"resume_id", "vacancy_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResumeInvitation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false)
    private Resume resume;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vacancy_id", nullable = false)
    private Vacancy vacancy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employer_id", nullable = false)
    private User employer; // Кто пригласил

    @Column(name = "message", columnDefinition = "TEXT")
    private String message; // Приглашение

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private InvitationStatus status = InvitationStatus.PENDING;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "viewed_at")
    private LocalDateTime viewedAt;

    public enum InvitationStatus {
        PENDING,    // Ожидает ответа
        VIEWED,     // Просмотрено
        ACCEPTED,   // Принято
        REJECTED    // Отклонено
    }
}
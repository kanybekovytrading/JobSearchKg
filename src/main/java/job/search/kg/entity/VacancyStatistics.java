package job.search.kg.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "vacancy_statistics")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VacancyStatistics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vacancy_id", nullable = false, unique = true)
    private Vacancy vacancy;

    @Column(name = "views_count", nullable = false)
    private Long viewsCount = 0L;

    @Column(name = "contact_clicks_count", nullable = false)
    private Long contactClicksCount = 0L;

    @Column(name = "response_count", nullable = false)
    private Long responseCount = 0L;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "last_view_at")
    private LocalDateTime lastViewAt;

    public void incrementViews() {
        this.viewsCount++;
        this.lastViewAt = LocalDateTime.now();
    }

    public void incrementContactClicks() {
        this.contactClicksCount++;
    }

    public void incrementResponses() {
        this.responseCount++;
    }
}
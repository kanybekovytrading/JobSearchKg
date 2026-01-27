package job.search.kg.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "social_tasks")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SocialTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private TaskType type;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "link", nullable = false, length = 500)
    private String link;

    @Column(name = "channel_id")
    private String channelId; // для Telegram (@channel_username)

    @Column(name = "reward", nullable = false)
    private Integer reward;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public enum TaskType {
        TELEGRAM,
        INSTAGRAM,
        FACEBOOK,
        TIKTOK,
        YOUTUBE
    }
}

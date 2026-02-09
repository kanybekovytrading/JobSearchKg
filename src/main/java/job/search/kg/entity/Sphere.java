package job.search.kg.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "spheres")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Sphere {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "name_ru", nullable = false, length = 100)
    private String nameRu;

    @Column(name = "name_en", nullable = false, length = 100)
    private String nameEn;

    @Column(name = "name_ky", nullable = false, length = 100)
    private String nameKy;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @JsonIgnore
    @OneToMany(mappedBy = "sphere", cascade = CascadeType.ALL)
    private List<Category> categories;

    // Вспомогательный метод для получения имени по языку
    public String getName(String language) {
        return switch (language.toUpperCase()) {
            case "EN" -> nameEn;
            case "KY" -> nameKy;
            default -> nameRu;
        };
    }
}
package job.search.kg.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "vacancies")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Vacancy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "salary", length = 100)
    private String salary;

    @Column(name = "company_name")
    private String companyName;

    @Column(name = "phone", length = 20)
    private String phone;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    @JoinColumn(name = "city_id", nullable = false)
    private City city;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    @JoinColumn(name = "subcategory_id", nullable = false)
    private Subcategory subcategory;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "min_age")
    private Integer minAge;

    @Column(name = "max_age")
    private Integer maxAge;

    @Enumerated(EnumType.STRING)
    @Column(name = "preferred_gender")
    private GenderPreference preferredGender = GenderPreference.ANY;

    private String address;

    private String schedule;

    private Integer experienceInYear;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @JsonIgnore
    @OneToOne(mappedBy = "vacancy", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private VacancyStatistics statistics;

    @JsonIgnore
    @OneToMany(mappedBy = "vacancy", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<VacancyMedia> vacancyMediaList;

    @JsonIgnore
    @OneToMany(mappedBy = "vacancy", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<VacancyBoost> vacancyBoosts;

    public enum GenderPreference {
        MALE,       // Только мужчины
        FEMALE,     // Только женщины
        ANY         // Без разницы
    }
}

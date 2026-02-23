package job.search.kg.repo;


import job.search.kg.dto.response.VacancyResponse;
import job.search.kg.entity.User;
import job.search.kg.entity.Vacancy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VacancyRepository extends JpaRepository<Vacancy, Long>, JpaSpecificationExecutor<Vacancy> {

    List<Vacancy> findByUser(User user);

    Long countByIsActive(Boolean isActive);

    Optional<Vacancy> findFirstByUserIdOrderByCreatedAtAsc(Long userId);

    @Query("""
        SELECT v
        FROM Vacancy v
        LEFT JOIN VacancyBoost b ON b.vacancy.id = v.id AND b.isActive = true AND b.expiresAt > CURRENT_TIMESTAMP
        LEFT JOIN VacancyStatistics vs ON vs.vacancy.id = v.id
        WHERE v.isActive = true
        ORDER BY (
            (1.0 / (1.0 + (CAST((CURRENT_TIMESTAMP - v.createdAt) AS double) / 604800000.0))) * 0.25 +
            (CAST(COALESCE(vs.contactClicksCount, 0) AS double) / CAST(GREATEST(COALESCE(vs.viewsCount, 0), 1) AS double) * 0.2) +
            (CASE WHEN :userCityId IS NULL THEN 0.0 
                  WHEN v.city.id = :userCityId THEN 0.25 
                  ELSE 0.05 END) +
            (CASE WHEN :subcategoryIds IS NULL THEN 0.0
                  WHEN v.subcategory.id IN :subcategoryIds THEN 0.3
                  ELSE 0.0 END) +
            (CASE WHEN b.id IS NOT NULL THEN 0.2 ELSE 0.0 END)
        ) DESC
    """)
    List<Vacancy> findRecommendedVacancies(
            @Param("userCityId") Long userCityId,
            @Param("subcategoryIds") List<Long> subcategoryIds,
            @Param("limit") int limit
    );

    // VacancyRepository
    List<Vacancy> findBySubcategoryIdAndCityIdAndIsActiveTrue(Integer subcategoryId, Integer cityId);

    @Query("SELECT v FROM Vacancy v JOIN FETCH v.user WHERE v.id = :id")
    Optional<Vacancy> findByIdWithUser(@Param("id") Long id);

    @Query("""
    SELECT v FROM Vacancy v WHERE v.user = :user
    ORDER BY
        CASE WHEN v.isActive = true THEN 0 ELSE 1 END,
        v.createdAt DESC
""")
    List<Vacancy> findByUserOrderByStatusAndDate(@Param("user") User user);
}
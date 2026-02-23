package job.search.kg.repo;

import job.search.kg.entity.Resume;
import job.search.kg.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResumeRepository extends JpaRepository<Resume, Long>, JpaSpecificationExecutor<Resume> {

    List<Resume> findByUser(User user);

    Long countByIsActive(Boolean isActive);

    Optional<Resume> findFirstByUserIdOrderByCreatedAtAsc(Long userId);
    /**
     * Найти активные резюме по подкатегории и городу
     * Используется для отправки уведомлений о новых вакансиях
     */
    @Query("SELECT r FROM Resume r " +
            "WHERE r.subcategory.id = :subcategoryId " +
            "AND r.city.id = :cityId " +
            "AND r.isActive = true")
    List<Resume> findBySubcategoryIdAndCityIdAndIsActiveTrue(
            @Param("subcategoryId") Integer subcategoryId,
            @Param("cityId") Integer cityId
    );

    List<Resume> findByUserAndIsActiveTrue(User user);

    @Query("SELECT r FROM Resume r JOIN FETCH r.user WHERE r.id = :id")
    Optional<Resume> findByIdWithUser(@Param("id") Long id);

}
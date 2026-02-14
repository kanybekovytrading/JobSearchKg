package job.search.kg.repo;

import job.search.kg.entity.Sphere;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SphereRepository extends JpaRepository<Sphere, Integer> {

    // Получить все активные сферы с сортировкой по имени
    List<Sphere> findByIsActive(Boolean isActive);

    // Или с явным query
    @Query("SELECT s FROM Sphere s WHERE s.isActive = :isActive")
    List<Sphere> findAllActiveSpheres(@Param("isActive") Boolean isActive);
}

package job.search.kg.repo;

import job.search.kg.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {

    @Query("SELECT c FROM Category c WHERE c.sphere.id = :sphereId AND c.isActive = :isActive")
    List<Category> findBySphereIdAndIsActive(
            @Param("sphereId") Integer sphereId,
            @Param("isActive") Boolean isActive
    );
}
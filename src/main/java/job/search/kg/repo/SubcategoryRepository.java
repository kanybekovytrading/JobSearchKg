package job.search.kg.repo;

import job.search.kg.entity.Category;
import job.search.kg.entity.Subcategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubcategoryRepository extends JpaRepository<Subcategory, Integer> {

    List<Subcategory> findByCategoryAndIsActive(Category category, Boolean isActive);

    List<Subcategory> findByCategoryIdAndIsActive(Integer categoryId, Boolean isActive);
}

package job.search.kg.service.admin;

import job.search.kg.dto.request.admin.CreateCategoryRequest;
import job.search.kg.dto.request.admin.CreateSubcategoryRequest;
import job.search.kg.dto.response.SubcategoryResponse;
import job.search.kg.entity.Category;
import job.search.kg.entity.Subcategory;
import job.search.kg.exceptions.ResourceNotFoundException;
import job.search.kg.repo.CategoryRepository;
import job.search.kg.repo.SubcategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminCategoryService {

    private final CategoryRepository categoryRepository;
    private final SubcategoryRepository subcategoryRepository;

    // ========== КАТЕГОРИИ ==========

    @Transactional(readOnly = true)
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Category getCategoryById(Integer id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
    }

    @Transactional
    public Category createCategory(CreateCategoryRequest request) {
        Category category = new Category();
        category.setNameRu(request.getNameRu());
        category.setNameKy(request.getNameKy());
        category.setNameEn(request.getNameEn());
        category.setIcon(request.getIcon());
        category.setIsActive(true);

        return categoryRepository.save(category);
    }

    @Transactional
    public Category updateCategory(Integer id, CreateCategoryRequest request) {
        Category category = getCategoryById(id);

        if (request.getNameRu() != null) {
            category.setNameRu(request.getNameRu());
        }
        if (request.getNameEn() != null) {
            category.setNameEn(request.getNameEn());
        }
        if (request.getIcon() != null) {
            category.setIcon(request.getIcon());
        }
        if (request.getIsActive() != null) {
            category.setIsActive(request.getIsActive());
        }

        return categoryRepository.save(category);
    }

    @Transactional
    public void deleteCategory(Integer id) {
        Category category = getCategoryById(id);
        categoryRepository.delete(category);
    }

    // ========== ПОДКАТЕГОРИИ ==========

    @Transactional(readOnly = true)
    public List<SubcategoryResponse> getSubcategoriesByCategory(Integer categoryId) {
        List<Subcategory> subcategories  = subcategoryRepository.findByCategoryIdAndIsActive(categoryId, true);
        return subcategories.stream()
                .map(SubcategoryResponse::new)
                .toList();
    }

    @Transactional(readOnly = true)
    public Subcategory getSubcategoryById(Integer id) {

        return subcategoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subcategory not found"));
    }

    @Transactional
    public SubcategoryResponse createSubcategory(CreateSubcategoryRequest request) {
        Category category = getCategoryById(request.getCategoryId());

        Subcategory subcategory = new Subcategory();
        subcategory.setCategory(category);
        subcategory.setNameRu(request.getNameRu());
        subcategory.setNameKy(request.getNameKy());
        subcategory.setNameEn(request.getNameEn());
        subcategory.setIsActive(true);

        Subcategory saved = subcategoryRepository.save(subcategory);
        return new SubcategoryResponse(saved);    }

    @Transactional
    public SubcategoryResponse updateSubcategory(Integer id, CreateSubcategoryRequest request) {
        Subcategory subcategory = getSubcategoryById(id);

        if (request.getNameRu() != null) {
            subcategory.setNameRu(request.getNameRu());
        }
        if (request.getNameEn() != null) {
            subcategory.setNameEn(request.getNameEn());
        }
        if (request.getIsActive() != null) {
            subcategory.setIsActive(request.getIsActive());
        }

        Subcategory updated = subcategoryRepository.save(subcategory);
        return new SubcategoryResponse(updated);
    }

    @Transactional
    public void deleteSubcategory(Integer id) {
        Subcategory subcategory = subcategoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subcategory not found"));

        subcategoryRepository.delete(subcategory);
    }
}

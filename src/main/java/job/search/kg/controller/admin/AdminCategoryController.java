package job.search.kg.controller.admin;

import job.search.kg.dto.request.admin.CreateCategoryRequest;
import job.search.kg.dto.request.admin.CreateSubcategoryRequest;
import job.search.kg.dto.response.SubcategoryResponse;
import job.search.kg.entity.Category;
import job.search.kg.entity.Subcategory;
import job.search.kg.service.admin.AdminCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/categories")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminCategoryController {

    private final AdminCategoryService adminCategoryService;

    // ========== КАТЕГОРИИ ==========

    @GetMapping
    public ResponseEntity<List<Category>> getAllCategories() {
        List<Category> categories = adminCategoryService.getAllCategories();
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Category> getCategoryById(@PathVariable Integer id) {
        Category category = adminCategoryService.getCategoryById(id);
        return ResponseEntity.ok(category);
    }

    @PostMapping
    public ResponseEntity<Category> createCategory(@RequestBody CreateCategoryRequest request) {
        Category category = adminCategoryService.createCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(category);
    }
    @PutMapping("/{id}")
    public ResponseEntity<Category> updateCategory(
            @PathVariable Integer id,
            @RequestBody CreateCategoryRequest request) {
        Category category = adminCategoryService.updateCategory(id, request);
        return ResponseEntity.ok(category);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Integer id) {
        adminCategoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }

// ========== ПОДКАТЕГОРИИ ==========

    @GetMapping("/{categoryId}/subcategories")
    public ResponseEntity<List<SubcategoryResponse>> getSubcategoriesByCategory(@PathVariable Integer categoryId) {
        List<SubcategoryResponse> subcategories = adminCategoryService.getSubcategoriesByCategory(categoryId);
        return ResponseEntity.ok(subcategories);
    }

    @PostMapping("/subcategories")
    public ResponseEntity<SubcategoryResponse> createSubcategory(@RequestBody CreateSubcategoryRequest request) {
        SubcategoryResponse subcategory = adminCategoryService.createSubcategory(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(subcategory);
    }

    @PutMapping("/subcategories/{id}")
    public ResponseEntity<SubcategoryResponse> updateSubcategory(
            @PathVariable Integer id,
            @RequestBody CreateSubcategoryRequest request) {
        SubcategoryResponse subcategory = adminCategoryService.updateSubcategory(id, request);
        return ResponseEntity.ok(subcategory);
    }

    @DeleteMapping("/subcategories/{id}")
    public ResponseEntity<Void> deleteSubcategory(@PathVariable Integer id) {
        adminCategoryService.deleteSubcategory(id);
        return ResponseEntity.noContent().build();
    }
}


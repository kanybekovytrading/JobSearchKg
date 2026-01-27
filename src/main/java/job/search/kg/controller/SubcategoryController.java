package job.search.kg.controller;

import job.search.kg.dto.response.CustomResponse;
import job.search.kg.entity.Subcategory;
import job.search.kg.service.SubcategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subcategories")
@RequiredArgsConstructor
public class SubcategoryController {

    private final SubcategoryService subcategoryService;

    @GetMapping("/category/{telegramId}/{categoryId}")
    public ResponseEntity<List<CustomResponse>> getSubcategoriesByCategory(@PathVariable Integer categoryId,@PathVariable Long telegramId) {
        List<CustomResponse> subcategories = subcategoryService.getSubcategoriesByCategory(categoryId, telegramId);
        return ResponseEntity.ok(subcategories);
    }

    @GetMapping("/{telegramId}/{id}")
    public ResponseEntity<CustomResponse> getSubcategoryById(@PathVariable Integer id,@PathVariable Long telegramId) {
        CustomResponse subcategory = subcategoryService.getSubcategoryById(id, telegramId);
        return ResponseEntity.ok(subcategory);
    }
}

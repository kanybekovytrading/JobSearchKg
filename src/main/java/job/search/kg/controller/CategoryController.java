package job.search.kg.controller;

import job.search.kg.dto.response.CustomResponse;
import job.search.kg.entity.Category;
import job.search.kg.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping("/{telegramId}")
    public ResponseEntity<List<CustomResponse>> getAllActiveCategories(@PathVariable Long telegramId) {
        List<CustomResponse> categories = categoryService.getAllActiveCategories(telegramId);
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/{telegramId}/{id}")
    public ResponseEntity<CustomResponse> getCategoryById(@PathVariable Integer id, @PathVariable Long telegramId) {
        CustomResponse category = categoryService.getCategoryById(id, telegramId);
        return ResponseEntity.ok(category);
    }
}

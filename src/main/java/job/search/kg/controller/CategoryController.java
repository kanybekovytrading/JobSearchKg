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

    @GetMapping("/sphere/{telegramId}")
    public ResponseEntity<List<CustomResponse>> getAllSpheres(
            @PathVariable Long telegramId
    ) {
        List<CustomResponse> spheres = categoryService.getAllSpheres(telegramId);
        return ResponseEntity.ok(spheres);
    }

    @GetMapping("/{telegramId}/{sphereId}")
    public ResponseEntity<List<CustomResponse>> getAllActiveCategories(@PathVariable Long telegramId, @PathVariable Integer sphereId) {
        List<CustomResponse> categories = categoryService.getAllActiveCategories(telegramId, sphereId);
        return ResponseEntity.ok(categories);
    }

    @GetMapping("admin/sphere")
    public ResponseEntity<List<CustomResponse>> getAllSpheres(
    ) {
        List<CustomResponse> spheres = categoryService.getAllSpheres(null);
        return ResponseEntity.ok(spheres);
    }

    @GetMapping("admin/{sphereId}")
    public ResponseEntity<List<CustomResponse>> getAllActiveCategories(@PathVariable Integer sphereId) {
        List<CustomResponse> categories = categoryService.getAllActiveCategories(null, sphereId);
        return ResponseEntity.ok(categories);
    }

}

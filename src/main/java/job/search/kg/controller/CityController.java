package job.search.kg.controller;

import job.search.kg.dto.response.CustomResponse;
import job.search.kg.service.CityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cities")
@RequiredArgsConstructor
public class CityController {

    private final CityService cityService;

    @GetMapping("/{telegramId}")
    public ResponseEntity<List<CustomResponse>> getAllActiveCities(@PathVariable Long telegramId) {
        List<CustomResponse> cities = cityService.getAllActiveCities(telegramId);
        return ResponseEntity.ok(cities);
    }

    @GetMapping("/{telegramId}/{id}")
    public ResponseEntity<CustomResponse> getCityById(@PathVariable Integer id, @PathVariable Long telegramId) {
        return ResponseEntity.ok(cityService.getCityById(id, telegramId));
    }
}

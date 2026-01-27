package job.search.kg.controller.admin;

import job.search.kg.dto.request.admin.CreateCityRequest;
import job.search.kg.entity.City;
import job.search.kg.service.admin.AdminCityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/cities")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminCityController {

    private final AdminCityService adminCityService;

    @GetMapping
    public ResponseEntity<List<City>> getAllCities() {
        List<City> cities = adminCityService.getAllCities();
        return ResponseEntity.ok(cities);
    }

    @GetMapping("/{id}")
    public ResponseEntity<City> getCityById(@PathVariable Integer id) {
        City city = adminCityService.getCityById(id);
        return ResponseEntity.ok(city);
    }

    @PostMapping
    public ResponseEntity<City> createCity(@RequestBody CreateCityRequest request) {
        City city = adminCityService.createCity(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(city);
    }

    @PutMapping("/{id}")
    public ResponseEntity<City> updateCity(
            @PathVariable Integer id,
            @RequestBody CreateCityRequest request) {
        City city = adminCityService.updateCity(id, request);
        return ResponseEntity.ok(city);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCity(@PathVariable Integer id) {
        adminCityService.deleteCity(id);
        return ResponseEntity.noContent().build();
    }
}

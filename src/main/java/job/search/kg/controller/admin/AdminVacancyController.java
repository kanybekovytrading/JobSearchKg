package job.search.kg.controller.admin;

import job.search.kg.dto.request.admin.CreateVacancyAdminRequest;
import job.search.kg.dto.request.admin.UpdateVacancyRequest;
import job.search.kg.entity.Vacancy;
import job.search.kg.service.admin.AdminVacancyService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/vacancies")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminVacancyController {

    private final AdminVacancyService adminVacancyService;

    @GetMapping
    public ResponseEntity<Page<Vacancy>> getAllVacancies(Pageable pageable) {
        Page<Vacancy> vacancies = adminVacancyService.getAllVacancies(pageable);
        return ResponseEntity.ok(vacancies);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Vacancy> getVacancyById(@PathVariable Long id) {
        Vacancy vacancy = adminVacancyService.getVacancyById(id);
        return ResponseEntity.ok(vacancy);
    }

    @PostMapping
    public ResponseEntity<Vacancy> createVacancy(
            @RequestBody CreateVacancyAdminRequest request,
            @RequestParam Long adminUserId) {
        Vacancy vacancy = adminVacancyService.createVacancy(request, adminUserId);
        return ResponseEntity.status(HttpStatus.CREATED).body(vacancy);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Vacancy> updateVacancy(
            @PathVariable Long id,
            @RequestBody UpdateVacancyRequest request) {
        Vacancy vacancy = adminVacancyService.updateVacancy(id, request);
        return ResponseEntity.ok(vacancy);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVacancy(@PathVariable Long id) {
        adminVacancyService.deleteVacancy(id);
        return ResponseEntity.noContent().build();
    }
}

package job.search.kg.controller.admin;

import job.search.kg.dto.request.admin.CreateVacancyAdminRequest;
import job.search.kg.dto.request.admin.UpdateVacancyRequest;
import job.search.kg.dto.response.admin.VacancyResponse;
import job.search.kg.entity.Vacancy;
import job.search.kg.mapper.VacancyMapper;
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

    private final VacancyMapper vacancyMapper;

    @GetMapping
    public ResponseEntity<Page<VacancyResponse>> getAllVacancies(
            Pageable pageable) {

        Page<Vacancy> vacancies = adminVacancyService.getAllVacancies(pageable);
        Page<VacancyResponse> response = vacancies.map(vacancyMapper::toResponse);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<VacancyResponse> getVacancyById(
            @PathVariable Long id) {

        Vacancy vacancy = adminVacancyService.getVacancyById(id);
        VacancyResponse response = vacancyMapper.toResponse(vacancy);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<VacancyResponse> createVacancy(
            @RequestBody CreateVacancyAdminRequest request,
            @RequestParam Long adminUserId) {

        Vacancy vacancy = adminVacancyService.createVacancy(request, adminUserId);
        VacancyResponse response = vacancyMapper.toResponse(vacancy);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<VacancyResponse> updateVacancy(
            @PathVariable Long id,
            @RequestBody UpdateVacancyRequest request) {

        Vacancy vacancy = adminVacancyService.updateVacancy(id, request);
        VacancyResponse response = vacancyMapper.toResponse(vacancy);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVacancy(@PathVariable Long id) {
        adminVacancyService.deleteVacancy(id);
        return ResponseEntity.noContent().build();
    }
}

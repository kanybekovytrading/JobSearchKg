package job.search.kg.controller.admin;

import job.search.kg.dto.request.admin.CreateVacancyAdminRequest;
import job.search.kg.dto.request.admin.UpdateVacancyRequest;
import job.search.kg.dto.response.admin.VacancyResponse;
import job.search.kg.entity.User;
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
            Pageable pageable,
            @RequestHeader(value = "Accept-Language", defaultValue = "ru") String languageHeader) {

        User.Language language = parseLanguage(languageHeader);
        Page<Vacancy> vacancies = adminVacancyService.getAllVacancies(pageable);
        Page<VacancyResponse> response = vacancies.map(v -> vacancyMapper.toResponse(v, language));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<VacancyResponse> getVacancyById(
            @PathVariable Long id,
            @RequestHeader(value = "Accept-Language", defaultValue = "ru") String languageHeader) {

        User.Language language = parseLanguage(languageHeader);
        Vacancy vacancy = adminVacancyService.getVacancyById(id);
        VacancyResponse response = vacancyMapper.toResponse(vacancy, language);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<VacancyResponse> createVacancy(
            @RequestBody CreateVacancyAdminRequest request,
            @RequestParam Long adminUserId,
            @RequestHeader(value = "Accept-Language", defaultValue = "ru") String languageHeader) {

        User.Language language = parseLanguage(languageHeader);
        Vacancy vacancy = adminVacancyService.createVacancy(request, adminUserId);
        VacancyResponse response = vacancyMapper.toResponse(vacancy, language);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<VacancyResponse> updateVacancy(
            @PathVariable Long id,
            @RequestBody UpdateVacancyRequest request,
            @RequestHeader(value = "Accept-Language", defaultValue = "ru") String languageHeader) {

        User.Language language = parseLanguage(languageHeader);
        Vacancy vacancy = adminVacancyService.updateVacancy(id, request);
        VacancyResponse response = vacancyMapper.toResponse(vacancy, language);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVacancy(@PathVariable Long id) {
        adminVacancyService.deleteVacancy(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Parse language from Accept-Language header
     * Supports: ru, ky, en
     * Defaults to RU if not recognized
     */
    private User.Language parseLanguage(String languageHeader) {
        if (languageHeader == null || languageHeader.isEmpty()) {
            return User.Language.RU;
        }

        // Extract first language code (e.g., "en-US" -> "en")
        String lang = languageHeader.split("[,;-]")[0].toLowerCase().trim();

        return switch (lang) {
            case "en" -> User.Language.EN;
            case "ky" -> User.Language.KY;
            case "ru" -> User.Language.RU;
            default -> User.Language.RU;
        };
    }
}

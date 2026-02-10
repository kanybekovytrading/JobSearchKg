package job.search.kg.controller.user;

import io.swagger.v3.oas.annotations.Operation;
import job.search.kg.dto.response.user.CreatePaymentResponse;
import job.search.kg.entity.ResumeBoost;
import job.search.kg.entity.VacancyBoost;
import job.search.kg.service.user.BoostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bot/boost")
@RequiredArgsConstructor
public class BotBoostController {
    private final BoostService boostService;

    @Operation(
            summary = "Поднять вакансию за баллы",
            description = "Стоимость: 200 баллов за 24 часа. Вакансия будет отображаться в топе результатов поиска."
    )
    @PostMapping("/vacancies/{vacancyId}/points")
    public ResponseEntity<VacancyBoost> boostVacancyWithPoints(
            @RequestParam Long telegramId,
            @PathVariable Long vacancyId) {
        return ResponseEntity.ok(boostService.boostVacancyWithPoints(telegramId, vacancyId));
    }

    @Operation(
            summary = "Поднять вакансию за деньги",
            description = "Стоимость: 20 сом за 24 часа. Вакансия будет отображаться в топе результатов поиска."
    )
    @PostMapping("/vacancies/{vacancyId}/money")
    public ResponseEntity<CreatePaymentResponse> boostVacancyWithMoney(
            @RequestParam Long telegramId,
            @PathVariable Long vacancyId) throws Exception {
        return ResponseEntity.ok(
                boostService.boostVacancyWithMoney(telegramId, vacancyId)
        );
    }

    @Operation(
            summary = "Поднять резюме за баллы",
            description = "Стоимость: 200 баллов за 24 часа. Резюме будет отображаться в топе результатов поиска."
    )
    @PostMapping("/resumes/{resumeId}/points")
    public ResponseEntity<ResumeBoost> boostResumeWithPoints(
            @RequestParam Long telegramId,
            @PathVariable Long resumeId) {
        return ResponseEntity.ok(boostService.boostResumeWithPoints(telegramId, resumeId));
    }

    @Operation(
            summary = "Поднять резюме за деньги",
            description = "Стоимость: 20 сом за 24 часа. Резюме будет отображаться в топе результатов поиска."
    )
    @PostMapping("/resumes/{resumeId}/money")
    public ResponseEntity<CreatePaymentResponse> boostResumeWithMoney(
            @RequestParam Long telegramId,
            @PathVariable Long resumeId) throws Exception {
        return ResponseEntity.ok(
                boostService.boostResumeWithMoney(telegramId, resumeId)
        );
    }

    @Operation(
            summary = "Проверить статус Boost вакансии",
            description = "Возвращает true, если Boost активен в данный момент"
    )
    @GetMapping("/vacancies/{vacancyId}/status")
    public ResponseEntity<Boolean> checkVacancyBoostStatus(@PathVariable Long vacancyId) {
        return ResponseEntity.ok(boostService.hasActiveVacancyBoost(vacancyId));
    }

    @Operation(
            summary = "Проверить статус Boost резюме",
            description = "Возвращает true, если Boost активен в данный момент"
    )
    @GetMapping("/resumes/{resumeId}/status")
    public ResponseEntity<Boolean> checkResumeBoostStatus(@PathVariable Long resumeId) {
        return ResponseEntity.ok(boostService.hasActiveResumeBoost(resumeId));
    }


}

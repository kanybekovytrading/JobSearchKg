package job.search.kg.controller.user;

import io.swagger.v3.oas.annotations.Operation;
import job.search.kg.entity.ResumeInvitation;
import job.search.kg.entity.VacancyResponse;
import job.search.kg.service.user.ResponseInvitationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bot/response")
@RequiredArgsConstructor
public class BotResponseController {
    private final ResponseInvitationService responseInvitationService;

    @Operation(
            summary = "Откликнуться на вакансию",
            description = "Отправить отклик на вакансию со своим резюме. " +
                    "Автоматически проверяются требования вакансии (возраст, пол)."
    )
    @PostMapping("/vacancies/{vacancyId}/respond")
    public ResponseEntity<VacancyResponse> respondToVacancy(
            @RequestParam Long telegramId,
            @PathVariable Long vacancyId,
            @RequestParam Long resumeId,
            @RequestParam(required = false) String message) {
        return ResponseEntity.ok(
                responseInvitationService.respondToVacancy(telegramId, vacancyId, resumeId, message)
        );
    }

    @Operation(
            summary = "Пригласить на собеседование",
            description = "Отправить приглашение кандидату на собеседование по его резюме"
    )
    @PostMapping("/resumes/{resumeId}/invite")
    public ResponseEntity<ResumeInvitation> inviteToInterview(
            @RequestParam Long telegramId,
            @PathVariable Long resumeId,
            @RequestParam Long vacancyId,
            @RequestParam(required = false) String message) {
        return ResponseEntity.ok(
                responseInvitationService.inviteToInterview(telegramId, resumeId, vacancyId, message)
        );
    }

    @Operation(
            summary = "Получить отклики на вакансию",
            description = "Получить список всех откликов на вакансию (только для владельца вакансии)"
    )
    @GetMapping("/vacancies/{vacancyId}/responses")
    public ResponseEntity<List<VacancyResponse>> getVacancyResponses(
            @RequestParam Long telegramId,
            @PathVariable Long vacancyId) {
        return ResponseEntity.ok(
                responseInvitationService.getVacancyResponses(telegramId, vacancyId)
        );
    }

    @Operation(
            summary = "Получить приглашения на резюме",
            description = "Получить список всех приглашений на собеседование (только для владельца резюме)"
    )
    @GetMapping("/resumes/{resumeId}/invitations")
    public ResponseEntity<List<ResumeInvitation>> getResumeInvitations(
            @RequestParam Long telegramId,
            @PathVariable Long resumeId) {
        return ResponseEntity.ok(
                responseInvitationService.getResumeInvitations(telegramId, resumeId)
        );
    }


    @Operation(
            summary = "Обновить статус отклика",
            description = "Изменить статус отклика: PENDING, VIEWED, ACCEPTED, REJECTED. " +
                    "Только владелец вакансии может менять статус."
    )
    @PutMapping("/responses/{responseId}/status")
    public ResponseEntity<VacancyResponse> updateResponseStatus(
            @RequestParam Long telegramId,
            @PathVariable Long responseId,
            @RequestParam VacancyResponse.ResponseStatus status) {
        return ResponseEntity.ok(
                responseInvitationService.updateResponseStatus(telegramId, responseId, status)
        );
    }


    @Operation(
            summary = "Обновить статус приглашения",
            description = "Изменить статус приглашения: PENDING, VIEWED, ACCEPTED, REJECTED. " +
                    "Только владелец резюме может менять статус."
    )
    @PutMapping("/invitations/{invitationId}/status")
    public ResponseEntity<ResumeInvitation> updateInvitationStatus(
            @RequestParam Long telegramId,
            @PathVariable Long invitationId,
            @RequestParam ResumeInvitation.InvitationStatus status) {
        return ResponseEntity.ok(
                responseInvitationService.updateInvitationStatus(telegramId, invitationId, status)
        );
    }
}

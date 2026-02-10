package job.search.kg.service.user;

import job.search.kg.entity.*;
import job.search.kg.exceptions.ResourceNotFoundException;
import job.search.kg.repo.*;
import job.search.kg.telegram.TelegramService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResponseInvitationService {

    private final VacancyResponseRepository vacancyResponseRepository;
    private final ResumeInvitationRepository resumeInvitationRepository;
    private final VacancyRepository vacancyRepository;
    private final ResumeRepository resumeRepository;
    private final UserRepository userRepository;
    private final VacancyStatisticsRepository vacancyStatisticsRepository;
    private final ResumeStatisticsRepository resumeStatisticsRepository;
    private final TelegramService telegramService;

    /**
     * Отклик на вакансию
     */
    @Transactional
    public VacancyResponse respondToVacancy(
            Long telegramId,
            Long vacancyId,
            Long resumeId,
            String message
    ) {
        User applicant = userRepository.findByTelegramId(telegramId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Vacancy vacancy = vacancyRepository.findById(vacancyId)
                .orElseThrow(() -> new ResourceNotFoundException("Vacancy not found"));

        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found"));

        // Проверка: резюме принадлежит пользователю
        if (!resume.getUser().getId().equals(applicant.getId())) {
            throw new IllegalStateException("You can only respond with your own resume");
        }

        // Проверка на дубликаты
        if (vacancyResponseRepository.existsByVacancyIdAndResumeId(vacancyId, resumeId)) {
            throw new IllegalStateException("You have already responded to this vacancy");
        }

        // Проверка фильтров вакансии
        if (!matchesVacancyRequirements(vacancy, resume)) {
            throw new IllegalStateException("You do not meet the vacancy requirements");
        }

        // Создаем отклик
        VacancyResponse response = VacancyResponse.builder()
                .vacancy(vacancy)
                .resume(resume)
                .applicant(applicant)
                .message(message)
                .status(VacancyResponse.ResponseStatus.PENDING)
                .build();

        response = vacancyResponseRepository.save(response);

        // Обновляем статистику
        VacancyStatistics stats = vacancyStatisticsRepository
                .findByVacancyId(vacancyId)
                .orElseThrow(() -> new ResourceNotFoundException("Vacancy statistics not found"));
        stats.incrementResponses();
        vacancyStatisticsRepository.save(stats);

        // Уведомляем работодателя
        notifyEmployerAboutResponse(vacancy.getUser().getTelegramId(), vacancy.getTitle(), resume.getName());

        return response;
    }

    /**
     * Приглашение на собеседование
     */
    @Transactional
    public ResumeInvitation inviteToInterview(
            Long telegramId,
            Long resumeId,
            Long vacancyId,
            String message
    ) {
        User employer = userRepository.findByTelegramId(telegramId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found"));

        Vacancy vacancy = vacancyRepository.findById(vacancyId)
                .orElseThrow(() -> new ResourceNotFoundException("Vacancy not found"));

        // Проверка: вакансия принадлежит работодателю
        if (!vacancy.getUser().getId().equals(employer.getId())) {
            throw new IllegalStateException("You can only invite with your own vacancy");
        }

        // Проверка на дубликаты
        if (resumeInvitationRepository.existsByResumeIdAndVacancyId(resumeId, vacancyId)) {
            throw new IllegalStateException("You have already invited this candidate");
        }

        // Создаем приглашение
        ResumeInvitation invitation = ResumeInvitation.builder()
                .resume(resume)
                .vacancy(vacancy)
                .employer(employer)
                .message(message)
                .status(ResumeInvitation.InvitationStatus.PENDING)
                .build();

        invitation = resumeInvitationRepository.save(invitation);

        // Обновляем статистику
        ResumeStatistics stats = resumeStatisticsRepository
                .findByResumeId(resumeId)
                .orElseThrow(() -> new ResourceNotFoundException("Resume statistics not found"));
        stats.incrementInvitations();
        resumeStatisticsRepository.save(stats);

        // Уведомляем соискателя
        notifyCandidateAboutInvitation(
                resume.getUser().getTelegramId(),
                vacancy.getTitle(),
                vacancy.getCompanyName()
        );

        return invitation;
    }

    /**
     * Получить отклики на вакансию (для работодателя)
     */
    @Transactional(readOnly = true)
    public List<VacancyResponse> getVacancyResponses(Long telegramId, Long vacancyId) {
        Vacancy vacancy = vacancyRepository.findById(vacancyId)
                .orElseThrow(() -> new ResourceNotFoundException("Vacancy not found"));

        User user = userRepository.findByTelegramId(telegramId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!vacancy.getUser().getId().equals(user.getId())) {
            throw new IllegalStateException("You can only view responses to your own vacancies");
        }

        return vacancyResponseRepository.findByVacancyIdOrderByCreatedAtDesc(vacancyId);
    }

    /**
     * Получить приглашения на резюме (для соискателя)
     */
    @Transactional(readOnly = true)
    public List<ResumeInvitation> getResumeInvitations(Long telegramId, Long resumeId) {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found"));

        User user = userRepository.findByTelegramId(telegramId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!resume.getUser().getId().equals(user.getId())) {
            throw new IllegalStateException("You can only view invitations to your own resume");
        }

        return resumeInvitationRepository.findByResumeIdOrderByCreatedAtDesc(resumeId);
    }

    /**
     * Изменить статус отклика
     */
    @Transactional
    public VacancyResponse updateResponseStatus(
            Long telegramId,
            Long responseId,
            VacancyResponse.ResponseStatus newStatus
    ) {
        VacancyResponse response = vacancyResponseRepository.findById(responseId)
                .orElseThrow(() -> new ResourceNotFoundException("Response not found"));

        User user = userRepository.findByTelegramId(telegramId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Только работодатель может менять статус
        if (!response.getVacancy().getUser().getId().equals(user.getId())) {
            throw new IllegalStateException("Only vacancy owner can change response status");
        }

        response.setStatus(newStatus);
        if (newStatus == VacancyResponse.ResponseStatus.VIEWED) {
            response.setViewedAt(LocalDateTime.now());
        }

        return vacancyResponseRepository.save(response);
    }

    /**
     * Изменить статус приглашения
     */
    @Transactional
    public ResumeInvitation updateInvitationStatus(
            Long telegramId,
            Long invitationId,
            ResumeInvitation.InvitationStatus newStatus
    ) {
        ResumeInvitation invitation = resumeInvitationRepository.findById(invitationId)
                .orElseThrow(() -> new ResourceNotFoundException("Invitation not found"));

        User user = userRepository.findByTelegramId(telegramId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Только владелец резюме может менять статус
        if (!invitation.getResume().getUser().getId().equals(user.getId())) {
            throw new IllegalStateException("Only resume owner can change invitation status");
        }

        invitation.setStatus(newStatus);
        if (newStatus == ResumeInvitation.InvitationStatus.VIEWED) {
            invitation.setViewedAt(LocalDateTime.now());
        }

        return resumeInvitationRepository.save(invitation);
    }

    /**
     * Проверка соответствия требованиям вакансии
     */
    private boolean matchesVacancyRequirements(Vacancy vacancy, Resume resume) {
        // Проверка возраста
        if (vacancy.getMinAge() != null && resume.getAge() < vacancy.getMinAge()) {
            return false;
        }
        if (vacancy.getMaxAge() != null && resume.getAge() > vacancy.getMaxAge()) {
            return false;
        }

        // Проверка пола
        if (vacancy.getPreferredGender() != Vacancy.GenderPreference.ANY) {
            if (vacancy.getPreferredGender() == Vacancy.GenderPreference.MALE &&
                    resume.getGender() != Resume.Gender.MALE) {
                return false;
            }
            return vacancy.getPreferredGender() != Vacancy.GenderPreference.FEMALE ||
                    resume.getGender() == Resume.Gender.FEMALE;
        }

        return true;
    }

    private void notifyEmployerAboutResponse(Long employerTelegramId, String vacancyTitle, String candidateName) {
        String message = String.format(
                "🔔 Новый отклик на вакансию \"%s\"!\n\nКандидат: %s\n\nПроверьте детали в разделе \"Мои вакансии\"",
                vacancyTitle,
                candidateName
        );
        telegramService.sendMessage(employerTelegramId, message);
    }

    private void notifyCandidateAboutInvitation(Long candidateTelegramId, String vacancyTitle, String companyName) {
        String message = String.format(
                "🎉 Вас пригласили на собеседование!\n\nВакансия: %s\nКомпания: %s\n\nПроверьте детали в разделе \"Мои резюме\"",
                vacancyTitle,
                companyName
        );
        telegramService.sendMessage(candidateTelegramId, message);
    }
}

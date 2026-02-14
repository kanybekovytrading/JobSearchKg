package job.search.kg.service.user;

import job.search.kg.dto.request.user.SearchRequest;
import job.search.kg.dto.response.MediaResponse;
import job.search.kg.dto.response.VacancyResponse;
import job.search.kg.dto.response.user.ResumeResponse;
import job.search.kg.dto.response.user.ResumeStatisticsResponse;
import job.search.kg.dto.response.user.SearchResultResponse;
import job.search.kg.dto.response.user.VacancyStatisticsResponse;
import job.search.kg.entity.*;
import job.search.kg.exceptions.ResourceNotFoundException;
import job.search.kg.repo.*;
import job.search.kg.service.LocationService;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BotSearchService {

    private final ResumeRepository resumeRepository;
    private final VacancyRepository vacancyRepository;
    private final BotAccessService accessService;
    private final VacancyBoostRepository vacancyBoostRepository;
    private final ResumeBoostRepository resumeBoostRepository;
    private final VacancyStatisticsRepository vacancyStatisticsRepository;
    private final ResumeStatisticsRepository resumeStatisticsRepository;
    private final ResumeMediaRepository resumeMediaRepository;
    private final VacancyMediaRepository vacancyMediaRepository;
    private final FreeAccessTrackingService freeAccessTrackingService;
    private final FreeAccessTrackingRepository freeAccessTrackingRepository;
    private final LocationService locationService;

    private static final int FREE_DAILY_LIMIT = 3;

    @Transactional
    public SearchResultResponse<VacancyResponse> searchVacancies(
            Long telegramId,
            SearchRequest request,
            Double userLatitude,
            Double userLongitude) {

        Specification<Vacancy> spec = buildVacancySpecification(
                request.getCityId(),
                request.getSphereId(),
                request.getCategoryId(),
                request.getSubcategoryId()
        );

        LocalDateTime now = LocalDateTime.now();
        Set<Long> boostedVacancyIds = vacancyBoostRepository
                .findActiveBoostVacancyIds(now);

        List<Vacancy> vacancies = vacancyRepository.findAll(spec);

        // Сортируем с учетом boost и расстояния
        vacancies = sortVacanciesByBoostAndDistance(
                vacancies,
                boostedVacancyIds,
                userLatitude,
                userLongitude
        );

        boolean hasSubscription = accessService.canSearchJobs(telegramId);

        List<VacancyResponse> responses = new ArrayList<>();

        if (hasSubscription) {
            for (Vacancy vacancy : vacancies) {
                responses.add(mapVacancyToResponse(vacancy, userLatitude, userLongitude));
            }
        } else {
            Set<Long> freeAccessVacancyIds = getFreeAccessVacancyIdsOptimized(
                    telegramId,
                    request.getCityId(),
                    request.getSphereId(),
                    request.getCategoryId(),
                    request.getSubcategoryId(),
                    vacancies
            );

            for (Vacancy vacancy : vacancies) {
                if (freeAccessVacancyIds.contains(vacancy.getId())) {
                    responses.add(mapVacancyToResponse(vacancy, userLatitude, userLongitude));
                } else {
                    responses.add(mapVacancyToResponseWithoutSubs(vacancy, userLatitude, userLongitude));
                }
            }
        }

        SearchResultResponse<VacancyResponse> result = new SearchResultResponse<>();
        result.setResults(responses);
        result.setTotal(responses.size());

        return result;
    }

    @Transactional
    public SearchResultResponse<ResumeResponse> searchResumes(Long telegramId, SearchRequest request) {

        Specification<Resume> spec = buildResumeSpecification(
                request.getCityId(),
                request.getSphereId(),
                request.getCategoryId(),
                request.getSubcategoryId()
        );

        LocalDateTime now = LocalDateTime.now();
        Set<Long> boostedResumeIds = resumeBoostRepository
                .findActiveBoostResumeIds(now);

        List<Resume> resumes = resumeRepository.findAll(spec);

        resumes = sortResumesByBoostOptimized(resumes, boostedResumeIds);

        boolean hasSubscription = accessService.canSearchEmployees(telegramId);

        List<ResumeResponse> responses = new ArrayList<>();

        if (hasSubscription) {
            for (Resume resume : resumes) {
                responses.add(mapResumeToResponse(resume));
            }
        } else {
            Set<Long> freeAccessResumeIds = getFreeAccessResumeIdsOptimized(
                    telegramId,
                    request.getCityId(),
                    request.getSphereId(),
                    request.getCategoryId(),
                    request.getSubcategoryId(),
                    resumes
            );

            for (Resume resume : resumes) {
                if (freeAccessResumeIds.contains(resume.getId())) {
                    responses.add(mapResumeToResponse(resume));
                } else {
                    responses.add(mapResumeToResponseWithoutSubs(resume));
                }
            }
        }

        SearchResultResponse<ResumeResponse> result = new SearchResultResponse<>();
        result.setResults(responses);
        result.setTotal(responses.size());

        return result;
    }

    /**
     * Сортировка вакансий с учетом boost и расстояния
     */
    private List<Vacancy> sortVacanciesByBoostAndDistance(
            List<Vacancy> vacancies,
            Set<Long> boostedIds,
            Double userLat,
            Double userLon) {

        return vacancies.stream()
                .sorted((v1, v2) -> {
                    boolean v1HasBoost = boostedIds.contains(v1.getId());
                    boolean v2HasBoost = boostedIds.contains(v2.getId());

                    // Сначала сортируем по boost
                    if (v1HasBoost && !v2HasBoost) return -1;
                    if (!v1HasBoost && v2HasBoost) return 1;

                    // Если оба с boost или оба без boost, сортируем по расстоянию
                    if (userLat != null && userLon != null) {
                        Double dist1 = calculateDistanceForVacancy(v1, userLat, userLon);
                        Double dist2 = calculateDistanceForVacancy(v2, userLat, userLon);

                        // Если оба имеют координаты - сортируем по расстоянию
                        if (dist1 != null && dist2 != null) {
                            return Double.compare(dist1, dist2);
                        }

                        // Вакансии с координатами выше
                        if (dist1 != null) return -1;
                        if (dist2 != null) return 1;
                    }

                    // По умолчанию - по дате создания
                    return v2.getCreatedAt().compareTo(v1.getCreatedAt());
                })
                .collect(Collectors.toList());
    }

    /**
     * Вспомогательный метод для расчета расстояния до вакансии
     */
    private Double calculateDistanceForVacancy(Vacancy vacancy, Double userLat, Double userLon) {
        if (vacancy.getLatitude() != null && vacancy.getLongitude() != null) {
            return locationService.calculateDistance(
                    userLat, userLon,
                    vacancy.getLatitude(), vacancy.getLongitude()
            );
        }
        return null;
    }

    /**
     * Маппинг вакансии с расстоянием
     */
    public VacancyResponse mapVacancyToResponse(Vacancy vacancy, Double userLat, Double userLon) {
        VacancyResponse response = new VacancyResponse();
        response.setPhone(vacancy.getPhone());

        // Добавляем расстояние
        if (userLat != null && userLon != null &&
                vacancy.getLatitude() != null && vacancy.getLongitude() != null) {

            double distance = locationService.calculateDistance(
                    userLat, userLon,
                    vacancy.getLatitude(), vacancy.getLongitude()
            );
            response.setDistanceKm(distance);
        }

        return getVacancyResponse(vacancy, response);
    }

    public VacancyResponse mapVacancyToResponseWithoutSubs(Vacancy vacancy, Double userLat, Double userLon) {
        VacancyResponse response = new VacancyResponse();

        if (vacancy.getPhone() != null && !vacancy.getPhone().isEmpty()) {
            String phone = vacancy.getPhone();
            String maskedPhone = phone.length() > 6 ? phone.substring(0, 6) + " *** ***" : "*** *** ***";
            response.setPhone(maskedPhone);
        }

        // Добавляем расстояние
        if (userLat != null && userLon != null &&
                vacancy.getLatitude() != null && vacancy.getLongitude() != null) {

            double distance = locationService.calculateDistance(
                    userLat, userLon,
                    vacancy.getLatitude(), vacancy.getLongitude()
            );
            response.setDistanceKm(distance);
        }

        return getVacancyResponse(vacancy, response);
    }

    // Старые методы без location параметров для обратной совместимости
    public VacancyResponse mapVacancyToResponse(Vacancy vacancy) {
        return mapVacancyToResponse(vacancy, null, null);
    }

    public VacancyResponse mapVacancyToResponseWithoutSubs(Vacancy vacancy) {
        return mapVacancyToResponseWithoutSubs(vacancy, null, null);
    }

    private Set<Long> getFreeAccessResumeIdsOptimized(
            Long telegramId,
            Integer cityId,
            Integer sphereId,
            Integer categoryId,
            Integer subcategoryId,
            List<Resume> sortedResumes) {

        LocalDate today = LocalDate.now();
        String searchKey = buildSearchKey("RESUME", cityId, sphereId, categoryId, subcategoryId);

        List<Long> cachedIds = freeAccessTrackingRepository
                .findTodayFreeAccessIds(telegramId, searchKey, today);

        if (!cachedIds.isEmpty() && cachedIds.size() >= FREE_DAILY_LIMIT) {
            return new HashSet<>(cachedIds.subList(0, FREE_DAILY_LIMIT));
        }

        List<Long> selectedIds = sortedResumes.stream()
                .limit(FREE_DAILY_LIMIT)
                .map(Resume::getId)
                .collect(Collectors.toList());

        if (cachedIds.isEmpty() && !selectedIds.isEmpty()) {
            freeAccessTrackingService.saveBatch(telegramId, searchKey, selectedIds, today);
        }

        return new HashSet<>(selectedIds);
    }

    protected Set<Long> getFreeAccessVacancyIdsOptimized(
            Long telegramId,
            Integer cityId,
            Integer sphereId,
            Integer categoryId,
            Integer subcategoryId,
            List<Vacancy> sortedVacancies) {

        LocalDate today = LocalDate.now();
        String searchKey = buildSearchKey("VACANCY", cityId, sphereId, categoryId, subcategoryId);

        List<Long> cachedIds = freeAccessTrackingRepository
                .findTodayFreeAccessIds(telegramId, searchKey, today);

        if (!cachedIds.isEmpty() && cachedIds.size() >= FREE_DAILY_LIMIT) {
            return new HashSet<>(cachedIds.subList(0, FREE_DAILY_LIMIT));
        }

        List<Long> selectedIds = sortedVacancies.stream()
                .limit(FREE_DAILY_LIMIT)
                .map(Vacancy::getId)
                .collect(Collectors.toList());

        if (cachedIds.isEmpty() && !selectedIds.isEmpty()) {
            freeAccessTrackingService.saveBatch(telegramId, searchKey, selectedIds, today);
        }

        return new HashSet<>(selectedIds);
    }

    private List<Resume> sortResumesByBoostOptimized(List<Resume> resumes, Set<Long> boostedIds) {
        return resumes.stream()
                .sorted((r1, r2) -> {
                    boolean r1HasBoost = boostedIds.contains(r1.getId());
                    boolean r2HasBoost = boostedIds.contains(r2.getId());

                    if (r1HasBoost && !r2HasBoost) return -1;
                    if (!r1HasBoost && r2HasBoost) return 1;

                    return r2.getCreatedAt().compareTo(r1.getCreatedAt());
                })
                .collect(Collectors.toList());
    }

    private String buildSearchKey(String type, Integer cityId, Integer sphereId,
                                  Integer categoryId, Integer subcategoryId) {
        return String.format("%s_C%d_S%d_CAT%d_SUB%d",
                type,
                cityId != null ? cityId : 0,
                sphereId != null ? sphereId : 0,
                categoryId != null ? categoryId : 0,
                subcategoryId != null ? subcategoryId : 0
        );
    }

    private Specification<Resume> buildResumeSpecification(
            Integer cityId, Integer sphereId, Integer categoryId, Integer subcategoryId) {

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("isActive"), true));

            if (cityId != null) {
                predicates.add(cb.equal(root.get("city").get("id"), cityId));
            }
            if (sphereId != null) {
                Join<Resume, Category> categoryJoin = root.join("category");
                predicates.add(cb.equal(categoryJoin.get("sphere").get("id"), sphereId));
            }
            if (categoryId != null) {
                predicates.add(cb.equal(root.get("category").get("id"), categoryId));
            }
            if (subcategoryId != null) {
                predicates.add(cb.equal(root.get("subcategory").get("id"), subcategoryId));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private Specification<Vacancy> buildVacancySpecification(
            Integer cityId, Integer sphereId, Integer categoryId, Integer subcategoryId) {

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("isActive"), true));

            if (cityId != null) {
                predicates.add(cb.equal(root.get("city").get("id"), cityId));
            }
            if (sphereId != null) {
                Join<Vacancy, Category> categoryJoin = root.join("category");
                predicates.add(cb.equal(categoryJoin.get("sphere").get("id"), sphereId));
            }
            if (categoryId != null) {
                predicates.add(cb.equal(root.get("category").get("id"), categoryId));
            }
            if (subcategoryId != null) {
                predicates.add(cb.equal(root.get("subcategory").get("id"), subcategoryId));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    @Transactional
    public void trackVacancyView(Long vacancyId) {
        Vacancy vacancy = vacancyRepository.findById(vacancyId)
                .orElseThrow(() -> new ResourceNotFoundException("Vacancy not found"));

        VacancyStatistics stats = vacancyStatisticsRepository
                .findByVacancyId(vacancyId)
                .orElseGet(() -> {
                    VacancyStatistics newStats = VacancyStatistics.builder()
                            .vacancy(vacancy)
                            .viewsCount(0L)
                            .contactClicksCount(0L)
                            .responseCount(0L)
                            .build();
                    return vacancyStatisticsRepository.save(newStats);
                });

        stats.incrementViews();
        vacancyStatisticsRepository.save(stats);
    }

    @Transactional
    public void trackVacancyContactClick(Long vacancyId) {
        VacancyStatistics stats = vacancyStatisticsRepository
                .findByVacancyId(vacancyId)
                .orElseThrow(() -> new ResourceNotFoundException("Vacancy statistics not found"));

        stats.incrementContactClicks();
        vacancyStatisticsRepository.save(stats);
    }

    @Transactional
    public void trackResumeView(Long resumeId) {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found"));

        ResumeStatistics stats = resumeStatisticsRepository
                .findByResumeId(resumeId)
                .orElseGet(() -> {
                    ResumeStatistics newStats = ResumeStatistics.builder()
                            .resume(resume)
                            .viewsCount(0L)
                            .contactClicksCount(0L)
                            .invitationCount(0L)
                            .build();
                    return resumeStatisticsRepository.save(newStats);
                });

        stats.incrementViews();
        resumeStatisticsRepository.save(stats);
    }

    @Transactional
    public void trackResumeContactClick(Long resumeId) {
        ResumeStatistics stats = resumeStatisticsRepository
                .findByResumeId(resumeId)
                .orElseThrow(() -> new ResourceNotFoundException("Resume statistics not found"));

        stats.incrementContactClicks();
        resumeStatisticsRepository.save(stats);
    }

    public ResumeResponse mapResumeToResponse(Resume resume) {
        ResumeResponse response = new ResumeResponse();
        response.setId(resume.getId());
        response.setName(resume.getName());
        response.setAge(resume.getAge());
        response.setGender(resume.getGender());
        response.setCityName(resume.getCity().getNameRu());
        response.setCategoryName(resume.getCategory().getNameRu());
        response.setSubcategoryName(resume.getSubcategory().getNameRu());
        response.setExperience(resume.getExperience());
        response.setDescription(resume.getDescription());
        response.setTelegramUsername(resume.getUser().getUsername());
        response.setPhone(resume.getUser().getPhone());

        List<MediaResponse> mediaList = resumeMediaRepository
                .findByResumeIdOrderByDisplayOrderAsc(resume.getId())
                .stream()
                .map(this::mapResumeMediaToResponse)
                .collect(Collectors.toList());
        response.setMedia(mediaList);

        return response;
    }

    public ResumeResponse mapResumeToResponseWithoutSubs(Resume resume) {
        ResumeResponse response = new ResumeResponse();

        if (resume.getUser().getPhone() != null && !resume.getUser().getPhone().isEmpty()) {
            String phone = resume.getUser().getPhone();
            String maskedPhone = phone.length() > 6 ? phone.substring(0, 6) + " *** ***" : "*** *** ***";
            response.setPhone(maskedPhone);
        }

        response.setId(resume.getId());
        response.setName(resume.getName());
        response.setAge(resume.getAge());
        response.setGender(resume.getGender());
        response.setCityName(resume.getCity().getNameRu());
        response.setCategoryName(resume.getCategory().getNameRu());
        response.setSubcategoryName(resume.getSubcategory().getNameRu());
        response.setExperience(resume.getExperience());
        response.setDescription(resume.getDescription());

        List<MediaResponse> mediaList = resumeMediaRepository
                .findByResumeIdOrderByDisplayOrderAsc(resume.getId())
                .stream()
                .map(this::mapResumeMediaToResponse)
                .collect(Collectors.toList());
        response.setMedia(mediaList);

        return response;
    }

    @Transactional(readOnly = true)
    public VacancyStatisticsResponse getVacancyStatistics(Long vacancyId) {
        Vacancy vacancy = vacancyRepository.findById(vacancyId)
                .orElseThrow(() -> new ResourceNotFoundException("Vacancy not found"));

        VacancyStatistics stats = vacancyStatisticsRepository
                .findByVacancyId(vacancyId)
                .orElseGet(() -> VacancyStatistics.builder()
                        .vacancy(vacancy)
                        .viewsCount(0L)
                        .contactClicksCount(0L)
                        .responseCount(0L)
                        .build());

        return VacancyStatisticsResponse.builder()
                .vacancyId(vacancy.getId())
                .vacancyTitle(vacancy.getTitle())
                .viewsCount(stats.getViewsCount())
                .contactClicksCount(stats.getContactClicksCount())
                .responseCount(stats.getResponseCount())
                .build();
    }

    @Transactional(readOnly = true)
    public ResumeStatisticsResponse getResumeStatistics(Long resumeId) {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found"));

        ResumeStatistics stats = resumeStatisticsRepository
                .findByResumeId(resumeId)
                .orElseGet(() -> ResumeStatistics.builder()
                        .resume(resume)
                        .viewsCount(0L)
                        .contactClicksCount(0L)
                        .invitationCount(0L)
                        .build());

        return ResumeStatisticsResponse.builder()
                .resumeId(resume.getId())
                .resumeName(resume.getName())
                .viewsCount(stats.getViewsCount())
                .contactClicksCount(stats.getContactClicksCount())
                .invitationCount(stats.getInvitationCount())
                .build();
    }

    @NonNull
    private VacancyResponse getVacancyResponse(Vacancy vacancy, VacancyResponse response) {
        response.setId(vacancy.getId());
        response.setTitle(vacancy.getTitle());
        response.setDescription(vacancy.getDescription());
        response.setSalary(vacancy.getSalary());
        response.setCompanyName(vacancy.getCompanyName());
        response.setCityName(vacancy.getCity().getNameRu());
        response.setCategoryName(vacancy.getCategory().getNameRu());
        response.setSubcategoryName(vacancy.getSubcategory().getNameRu());
        response.setCreatedAt(vacancy.getCreatedAt());
        response.setLatitude(vacancy.getLatitude());
        response.setLongitude(vacancy.getLongitude());

        if (response.getPhone() != null && !response.getPhone().contains("***")) {
            response.setTelegramUsername(vacancy.getUser().getUsername());
        }

        response.setExperienceInYear(vacancy.getExperienceInYear());
        response.setAddress(vacancy.getAddress());
        response.setMaxAge(vacancy.getMaxAge());
        response.setMinAge(vacancy.getMinAge());
        response.setPreferredGender(vacancy.getPreferredGender());
        response.setSchedule(vacancy.getSchedule());

        List<MediaResponse> mediaList = vacancyMediaRepository
                .findByVacancyIdOrderByDisplayOrderAsc(vacancy.getId())
                .stream()
                .map(this::mapVacancyMediaToResponse)
                .collect(Collectors.toList());
        response.setMedia(mediaList);

        return response;
    }

    private MediaResponse mapResumeMediaToResponse(ResumeMedia media) {
        return MediaResponse.builder()
                .id(media.getId())
                .mediaType(media.getMediaType().name())
                .fileUrl(media.getFileUrl())
                .fileName(media.getFileName())
                .fileSize(media.getFileSize())
                .displayOrder(media.getDisplayOrder())
                .uploadedAt(media.getUploadedAt())
                .build();
    }

    private MediaResponse mapVacancyMediaToResponse(VacancyMedia media) {
        return MediaResponse.builder()
                .id(media.getId())
                .mediaType(media.getMediaType().name())
                .fileUrl(media.getFileUrl())
                .fileName(media.getFileName())
                .fileSize(media.getFileSize())
                .displayOrder(media.getDisplayOrder())
                .uploadedAt(media.getUploadedAt())
                .build();
    }
}
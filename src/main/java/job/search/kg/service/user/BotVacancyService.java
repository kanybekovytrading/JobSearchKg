package job.search.kg.service.user;

import job.search.kg.dto.request.user.CreateVacancyRequest;
import job.search.kg.dto.response.MediaResponse;
import job.search.kg.dto.response.VacancyResponse;
import job.search.kg.dto.response.user.VacancyStatsResponse;
import job.search.kg.entity.*;
import job.search.kg.exceptions.ResourceNotFoundException;
import job.search.kg.repo.*;
import job.search.kg.service.MinioStorageService;
import job.search.kg.telegram.notification.VacancyNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BotVacancyService {

    private final VacancyRepository vacancyRepository;
    private final UserRepository userRepository;
    private final CityRepository cityRepository;
    private final CategoryRepository categoryRepository;
    private final SubcategoryRepository subcategoryRepository;
    private final VacancyMediaRepository vacancyMediaRepository;
    private final MinioStorageService minioStorageService;
    private final VacancyNotificationService notificationService;
    private final FreeAccessTrackingRepository freeAccessTrackingRepository;

    private static final int MAX_PHOTOS = 10;
    private static final int MAX_VIDEOS = 3;
    private static final long MAX_PHOTO_SIZE = 10 * 1024 * 1024; // 10 MB
    private static final long MAX_VIDEO_SIZE = 100 * 1024 * 1024; // 100 MB

    @Transactional(readOnly = true)
    public VacancyStatsResponse getUserVacancyStats(Long telegramId) {
        User user = userRepository.findByTelegramId(telegramId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<Vacancy> vacancies = vacancyRepository.findByUser(user);

        long totalCount = vacancies.size();
        long activeCount = vacancies.stream()
                .filter(Vacancy::getIsActive)
                .count();
        long inactiveCount = totalCount - activeCount;

        VacancyStatsResponse response = new VacancyStatsResponse();
        response.setTotalCount(totalCount);
        response.setActiveCount(activeCount);
        response.setInactiveCount(inactiveCount);

        return response;
    }

    @Transactional
    public Vacancy updateVacancyStatus(Long vacancyId, Long telegramId, Boolean isActive) {
        Vacancy vacancy = vacancyRepository.findById(vacancyId)
                .orElseThrow(() -> new ResourceNotFoundException("Vacancy not found"));

        if (!vacancy.getUser().getTelegramId().equals(telegramId)) {
            throw new AccessDeniedException("Access denied");
        }

        vacancy.setIsActive(isActive);
        return vacancyRepository.save(vacancy);
    }

    @Transactional
    public Vacancy createVacancy(Long telegramId, CreateVacancyRequest request) {
        User user = userRepository.findByTelegramId(telegramId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        City city = cityRepository.findById(request.getCityId())
                .orElseThrow(() -> new ResourceNotFoundException("City not found"));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        Subcategory subcategory = subcategoryRepository.findById(request.getSubcategoryId())
                .orElse(null);

        Vacancy vacancy = new Vacancy();
        vacancy.setUser(user);
        vacancy.setTitle(request.getTitle());
        vacancy.setDescription(request.getDescription());
        vacancy.setSalary(request.getSalary());
        vacancy.setCompanyName(request.getCompanyName());
        vacancy.setPhone(request.getPhone() != null ? request.getPhone() : user.getPhone());
        vacancy.setCity(city);
        vacancy.setCategory(category);
        vacancy.setSubcategory(subcategory);
        vacancy.setAddress(request.getAddress());
        vacancy.setPreferredGender(request.getPreferredGender());
        vacancy.setMinAge(request.getMinAge());
        vacancy.setMaxAge(request.getMaxAge());
        vacancy.setSchedule(request.getSchedule());
        vacancy.setExperienceInYear(request.getExperienceInYear());
        vacancy.setIsActive(true);
        vacancy.setLatitude(request.getLatitude());
        vacancy.setLongitude(request.getLongitude());

        Vacancy savedVacancy = vacancyRepository.save(vacancy);

        notificationService.notifyUsersAboutNewVacancy(savedVacancy);

        return savedVacancy;
    }

    @Transactional(readOnly = true)
    public List<VacancyResponse> getUserVacancies(Long telegramId) {
        User user = userRepository.findByTelegramId(telegramId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return vacancyRepository.findByUser(user).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteVacancy(Long vacancyId, Long telegramId) {
        Vacancy vacancy = vacancyRepository.findById(vacancyId)
                .orElseThrow(() -> new ResourceNotFoundException("Vacancy not found"));

        if (!vacancy.getUser().getTelegramId().equals(telegramId)) {
            throw new AccessDeniedException("Access denied");
        }


        // Удаляем все медиа файлы из MinIO
        List<VacancyMedia> mediaList = vacancyMediaRepository.findByVacancyIdOrderByDisplayOrderAsc(vacancyId);
        for (VacancyMedia media : mediaList) {
            try {
                String objectName = minioStorageService.extractObjectNameFromUrl(media.getFileUrl());
                String bucket = minioStorageService.extractBucketFromUrl(media.getFileUrl());
                if (objectName != null && bucket != null) {
                    minioStorageService.deleteFile(bucket, objectName);
                }
            } catch (Exception e) {
                // Логируем, но продолжаем
            }
        }

        freeAccessTrackingRepository.deleteByVacancyId(vacancyId);

        vacancyRepository.delete(vacancy);
    }

    @Transactional
    public Vacancy updateVacancy(Long vacancyId, Long telegramId, CreateVacancyRequest request) {
        Vacancy vacancy = vacancyRepository.findById(vacancyId)
                .orElseThrow(() -> new ResourceNotFoundException("Vacancy not found"));

        if (!vacancy.getUser().getTelegramId().equals(telegramId)) {
            throw new AccessDeniedException("Access denied");
        }

        if (request.getCityId() != null && !vacancy.getCity().getId().equals(request.getCityId())) {
            City city = cityRepository.findById(request.getCityId())
                    .orElseThrow(() -> new ResourceNotFoundException("City not found"));
            vacancy.setCity(city);
        }

        if (request.getCategoryId() != null && !vacancy.getCategory().getId().equals(request.getCategoryId())) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
            vacancy.setCategory(category);
        }

        if (request.getSubcategoryId() != null && !vacancy.getSubcategory().getId().equals(request.getSubcategoryId())) {
            Subcategory subcategory = subcategoryRepository.findById(request.getSubcategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Subcategory not found"));
            vacancy.setSubcategory(subcategory);
        }

        if (request.getTitle() != null) {
            vacancy.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            vacancy.setDescription(request.getDescription());
        }
        if (request.getSalary() != null) {
            vacancy.setSalary(request.getSalary());
        }
        if (request.getCompanyName() != null) {
            vacancy.setCompanyName(request.getCompanyName());
        }
        if (request.getPhone() != null) {
            vacancy.setPhone(request.getPhone());
        }
        if (request.getAddress() != null) {
            vacancy.setAddress(request.getAddress());
        }
        if (request.getPreferredGender() != null) {
            vacancy.setPreferredGender(request.getPreferredGender());
        }
        if (request.getMinAge() != null) {
            vacancy.setMinAge(request.getMinAge());
        }
        if (request.getMaxAge() != null) {
            vacancy.setMaxAge(request.getMaxAge());
        }
        if (request.getSchedule() != null) {
            vacancy.setSchedule(request.getSchedule());
        }
        if (request.getExperienceInYear() != null) {
            vacancy.setExperienceInYear(request.getExperienceInYear());
        }

        return vacancyRepository.save(vacancy);
    }

    /**
     * Добавление фото к вакансии
     */
    @Transactional
    public MediaResponse addVacancyPhoto(Long vacancyId, Long telegramId, MultipartFile file) throws Exception {
        Vacancy vacancy = validateVacancyOwnership(vacancyId, telegramId);

        long photoCount = vacancyMediaRepository.findByVacancyIdOrderByDisplayOrderAsc(vacancyId)
                .stream()
                .filter(m -> m.getMediaType() == VacancyMedia.MediaType.PHOTO)
                .count();

        if (photoCount >= MAX_PHOTOS) {
            throw new IllegalStateException("Maximum number of photos reached (" + MAX_PHOTOS + ")");
        }

        if (file.getSize() > MAX_PHOTO_SIZE) {
            throw new IllegalArgumentException("Photo size exceeds maximum allowed size (10 MB)");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Invalid file type. Only images are allowed");
        }

        String fileUrl = minioStorageService.uploadVacancyFile(file, vacancyId);

        int nextOrder = vacancyMediaRepository.findByVacancyIdOrderByDisplayOrderAsc(vacancyId)
                .stream()
                .mapToInt(VacancyMedia::getDisplayOrder)
                .max()
                .orElse(0) + 1;

        VacancyMedia media = VacancyMedia.builder()
                .vacancy(vacancy)
                .mediaType(VacancyMedia.MediaType.PHOTO)
                .fileUrl(fileUrl)
                .fileName(file.getOriginalFilename())
                .fileSize(file.getSize())
                .displayOrder(nextOrder)
                .build();

        media = vacancyMediaRepository.save(media);

        return mapMediaToResponse(media);
    }

    /**
     * Добавление видео к вакансии
     */
    @Transactional
    public MediaResponse addVacancyVideo(Long vacancyId, Long telegramId, MultipartFile file) throws Exception {
        Vacancy vacancy = validateVacancyOwnership(vacancyId, telegramId);

        long videoCount = vacancyMediaRepository.findByVacancyIdOrderByDisplayOrderAsc(vacancyId)
                .stream()
                .filter(m -> m.getMediaType() == VacancyMedia.MediaType.VIDEO)
                .count();

        if (videoCount >= MAX_VIDEOS) {
            throw new IllegalStateException("Maximum number of videos reached (" + MAX_VIDEOS + ")");
        }

        if (file.getSize() > MAX_VIDEO_SIZE) {
            throw new IllegalArgumentException("Video size exceeds maximum allowed size (100 MB)");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("video/")) {
            throw new IllegalArgumentException("Invalid file type. Only videos are allowed");
        }

        String fileUrl = minioStorageService.uploadVacancyFile(file, vacancyId);

        int nextOrder = vacancyMediaRepository.findByVacancyIdOrderByDisplayOrderAsc(vacancyId)
                .stream()
                .mapToInt(VacancyMedia::getDisplayOrder)
                .max()
                .orElse(0) + 1;

        VacancyMedia media = VacancyMedia.builder()
                .vacancy(vacancy)
                .mediaType(VacancyMedia.MediaType.VIDEO)
                .fileUrl(fileUrl)
                .fileName(file.getOriginalFilename())
                .fileSize(file.getSize())
                .displayOrder(nextOrder)
                .build();

        media = vacancyMediaRepository.save(media);

        return mapMediaToResponse(media);
    }

    /**
     * Получение всех медиа файлов вакансии
     */
    @Transactional(readOnly = true)
    public List<MediaResponse> getVacancyMedia(Long vacancyId) {
        return vacancyMediaRepository.findByVacancyIdOrderByDisplayOrderAsc(vacancyId)
                .stream()
                .map(this::mapMediaToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Удаление медиа файла
     */
    @Transactional
    public void deleteVacancyMedia(Long mediaId, Long telegramId) throws Exception {
        VacancyMedia media = vacancyMediaRepository.findById(mediaId)
                .orElseThrow(() -> new ResourceNotFoundException("Media not found"));

        validateVacancyOwnership(media.getVacancy().getId(), telegramId);

        String objectName = minioStorageService.extractObjectNameFromUrl(media.getFileUrl());
        String bucket = minioStorageService.extractBucketFromUrl(media.getFileUrl());

        if (objectName != null && bucket != null) {
            minioStorageService.deleteFile(bucket, objectName);
        }

        vacancyMediaRepository.delete(media);
    }

    /**
     * Изменение порядка отображения медиа
     */
    @Transactional
    public void updateMediaOrder(Long mediaId, Long telegramId, Integer newOrder) {
        VacancyMedia media = vacancyMediaRepository.findById(mediaId)
                .orElseThrow(() -> new ResourceNotFoundException("Media not found"));

        validateVacancyOwnership(media.getVacancy().getId(), telegramId);

        media.setDisplayOrder(newOrder);
        vacancyMediaRepository.save(media);
    }

    private Vacancy validateVacancyOwnership(Long vacancyId, Long telegramId) {
        Vacancy vacancy = vacancyRepository.findById(vacancyId)
                .orElseThrow(() -> new ResourceNotFoundException("Vacancy not found"));

        if (!vacancy.getUser().getTelegramId().equals(telegramId)) {
            throw new AccessDeniedException("Access denied");
        }

        return vacancy;
    }

    private VacancyResponse mapToResponse(Vacancy vacancy) {
        VacancyResponse response = new VacancyResponse();
        response.setId(vacancy.getId());
        response.setTitle(vacancy.getTitle());
        response.setDescription(vacancy.getDescription());
        response.setSalary(vacancy.getSalary());
        response.setCompanyName(vacancy.getCompanyName());
        response.setPhone(vacancy.getPhone());
        response.setCityName(vacancy.getCity().getNameRu());
        response.setCategoryName(vacancy.getCategory().getNameRu());
        response.setSubcategoryName(vacancy.getSubcategory().getNameRu());
        response.setIsActive(vacancy.getIsActive());
        response.setCreatedAt(vacancy.getCreatedAt());

        // Добавляем медиа файлы
        List<MediaResponse> mediaList = vacancyMediaRepository
                .findByVacancyIdOrderByDisplayOrderAsc(vacancy.getId())
                .stream()
                .map(this::mapMediaToResponse)
                .collect(Collectors.toList());
        response.setMedia(mediaList);

        return response;
    }

    private MediaResponse mapMediaToResponse(VacancyMedia media) {
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
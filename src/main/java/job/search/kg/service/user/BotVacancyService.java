package job.search.kg.service.user;

import job.search.kg.controller.user.BotVacancyController;
import job.search.kg.dto.request.user.CreateVacancyRequest;
import job.search.kg.dto.response.MediaResponse;
import job.search.kg.dto.response.VacancyResponse;
import job.search.kg.dto.response.user.VacancyStatsResponse;
import job.search.kg.entity.*;
import job.search.kg.exceptions.ResourceNotFoundException;
import job.search.kg.repo.*;
import job.search.kg.service.MinioStorageService;
import job.search.kg.telegram.notification.VacancyNotificationService;
import job.search.kg.util.ByteArrayMultipartFile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Slf4j
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
    @Autowired
    @Qualifier("mediaUploadExecutor")
    private Executor mediaUploadExecutor;

    private static final int MAX_PHOTOS = 10;
    private static final int MAX_VIDEOS = 3;
    private static final long MAX_PHOTO_SIZE = 10 * 1024 * 1024; // 10 MB
    private static final long MAX_VIDEO_SIZE = 100 * 1024 * 1024; // 100 MB

    @Transactional(readOnly = true)
    public VacancyStatsResponse getUserVacancyStats(Long telegramId) {
        User user = userRepository.findByTelegramId(telegramId)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));

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
                .orElseThrow(() -> new ResourceNotFoundException("Вакансия не найдена"));

        if (!vacancy.getUser().getTelegramId().equals(telegramId)) {
            throw new AccessDeniedException("Доступ запрещён");
        }

        vacancy.setIsActive(isActive);
        return vacancyRepository.save(vacancy);
    }

    @Transactional
    public Vacancy createVacancy(Long telegramId, CreateVacancyRequest request) {
        User user = userRepository.findByTelegramId(telegramId)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));

        City city = cityRepository.findById(request.getCityId())
                .orElseThrow(() -> new ResourceNotFoundException("Город не найденd"));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Категория не найдена"));

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
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));

        return vacancyRepository.findByUserOrderByStatusAndDate(user).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteVacancy(Long vacancyId, Long telegramId) {
        Vacancy vacancy = vacancyRepository.findById(vacancyId)
                .orElseThrow(() -> new ResourceNotFoundException("Вакансия не найдена"));

        if (!vacancy.getUser().getTelegramId().equals(telegramId)) {
            throw new AccessDeniedException("Доступ запрещён");
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
        vacancyMediaRepository.deleteAll(mediaList);

        freeAccessTrackingRepository.deleteByVacancyId(vacancyId);

        vacancyRepository.delete(vacancy);
    }

    @Transactional
    public Vacancy updateVacancy(Long vacancyId, Long telegramId, CreateVacancyRequest request) {
        Vacancy vacancy = vacancyRepository.findById(vacancyId)
                .orElseThrow(() -> new ResourceNotFoundException("Вакансия не найдена"));

        if (!vacancy.getUser().getTelegramId().equals(telegramId)) {
            throw new AccessDeniedException("Доступ запрещён");
        }

        if (request.getCityId() != null && !vacancy.getCity().getId().equals(request.getCityId())) {
            City city = cityRepository.findById(request.getCityId())
                    .orElseThrow(() -> new ResourceNotFoundException("Город не найден"));
            vacancy.setCity(city);
        }

        if (request.getCategoryId() != null && !vacancy.getCategory().getId().equals(request.getCategoryId())) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Категория не найдена"));
            vacancy.setCategory(category);
        }

        if (request.getSubcategoryId() != null && !vacancy.getSubcategory().getId().equals(request.getSubcategoryId())) {
            Subcategory subcategory = subcategoryRepository.findById(request.getSubcategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Подкатегория не найдена"));
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

    @Async("mediaUploadExecutor")
    public void addVacancyMediaBatchAsync(Long vacancyId, Long telegramId, List<BotVacancyController.FileData> files) {
        log.info("=== ASYNC СТАРТОВАЛ, поток: {}", Thread.currentThread().getName()); // ← добавь
        try {
            // Валидация владельца
            Vacancy vacancy = validateVacancyOwnership(vacancyId, telegramId);

            List<VacancyMedia> existingMedia = vacancyMediaRepository
                    .findByVacancyIdOrderByDisplayOrderAsc(vacancyId);
            long currentPhotoCount = existingMedia.stream()
                    .filter(m -> m.getMediaType() == VacancyMedia.MediaType.PHOTO).count();
            long currentVideoCount = existingMedia.stream()
                    .filter(m -> m.getMediaType() == VacancyMedia.MediaType.VIDEO).count();
            int baseOrder = existingMedia.stream()
                    .mapToInt(VacancyMedia::getDisplayOrder).max().orElse(0);

            // Валидация файлов
            long newPhotoCount = 0, newVideoCount = 0;
            for (BotVacancyController.FileData fd : files) {
                if (fd.contentType().startsWith("video/")) {
                    if (++newVideoCount + currentVideoCount > MAX_VIDEOS)
                        throw new IllegalStateException("Превышен лимит видео (" + MAX_VIDEOS + ")");
                    if (fd.size() > MAX_VIDEO_SIZE)
                        throw new IllegalArgumentException("Видео '" + fd.fileName() + "' превышает 100 МБ");
                } else if (fd.contentType().startsWith("image/")) {
                    if (++newPhotoCount + currentPhotoCount > MAX_PHOTOS)
                        throw new IllegalStateException("Превышен лимит фото (" + MAX_PHOTOS + ")");
                    if (fd.size() > MAX_PHOTO_SIZE)
                        throw new IllegalArgumentException("Фото '" + fd.fileName() + "' превышает 10 МБ");
                } else {
                    throw new IllegalArgumentException("Недопустимый тип: " + fd.fileName());
                }
            }

            // Параллельная загрузка в Minio
            List<CompletableFuture<UploadResult>> futures = new ArrayList<>();
            for (int i = 0; i < files.size(); i++) {
                final BotVacancyController.FileData fd = files.get(i);
                final int order = baseOrder + i + 1;
                futures.add(CompletableFuture.supplyAsync(() -> {
                    try {
                        ByteArrayMultipartFile mockFile =
                                new ByteArrayMultipartFile(fd.bytes(), fd.fileName(), fd.contentType());
                        String fileUrl = minioStorageService.uploadVacancyFile(mockFile, vacancyId);
                        return new UploadResult(fd, fileUrl, order);
                    } catch (Exception e) {
                        throw new RuntimeException("Ошибка загрузки: " + fd.fileName(), e);
                    }
                }, mediaUploadExecutor));
            }

            List<VacancyMedia> mediaList = futures.stream()
                    .map(f -> {
                        try { return f.get(60, TimeUnit.SECONDS); }
                        catch (Exception e) { throw new RuntimeException(e); }
                    })
                    .map(result -> VacancyMedia.builder()
                            .vacancy(vacancy)
                            .mediaType(result.fileData().contentType().startsWith("video/")
                                    ? VacancyMedia.MediaType.VIDEO : VacancyMedia.MediaType.PHOTO)
                            .fileUrl(result.fileUrl())
                            .fileName(result.fileData().fileName())
                            .fileSize(result.fileData().size())
                            .displayOrder(result.order())
                            .build())
                    .collect(Collectors.toList());

            vacancyMediaRepository.saveAll(mediaList);
            log.info("Batch завершён: {} файлов для вакансии {}", files.size(), vacancyId);

        } catch (Exception e) {
            log.error("Ошибка batch загрузки для вакансии {}", vacancyId, e);
        }
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
                .orElseThrow(() -> new ResourceNotFoundException("Медиафайл не найден"));

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
                .orElseThrow(() -> new ResourceNotFoundException("Медиафайл не найден"));

        validateVacancyOwnership(media.getVacancy().getId(), telegramId);

        media.setDisplayOrder(newOrder);
        vacancyMediaRepository.save(media);
    }

    private Vacancy validateVacancyOwnership(Long vacancyId, Long telegramId) {
        Vacancy vacancy = vacancyRepository.findByIdWithUser(vacancyId)
                .orElseThrow(() -> new ResourceNotFoundException("Вакансия не найдена"));

        if (!vacancy.getUser().getTelegramId().equals(telegramId)) {
            throw new AccessDeniedException("Доступ запрещён");
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
                .fileUrl(minioStorageService.resolveUrl(media.getFileUrl()))
                .fileName(media.getFileName())
                .fileSize(media.getFileSize())
                .displayOrder(media.getDisplayOrder())
                .uploadedAt(media.getUploadedAt())
                .build();
    }
    public record UploadResult(BotVacancyController.FileData fileData, String fileUrl, int order) {}

}
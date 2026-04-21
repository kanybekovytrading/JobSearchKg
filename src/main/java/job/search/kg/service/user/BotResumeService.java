package job.search.kg.service.user;

import job.search.kg.controller.user.BotVacancyController;
import job.search.kg.dto.request.user.CreateResumeRequest;
import job.search.kg.dto.response.MediaResponse;
import job.search.kg.dto.response.user.ResumeResponse;
import job.search.kg.dto.response.user.ResumeStatsResponse;
import job.search.kg.entity.*;
import job.search.kg.exceptions.ResourceNotFoundException;
import job.search.kg.repo.*;
import job.search.kg.service.MinioStorageService;
import job.search.kg.telegram.notification.ResumeNotificationService;
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BotResumeService {

    private final ResumeRepository resumeRepository;
    private final UserRepository userRepository;
    private final CityRepository cityRepository;
    private final CategoryRepository categoryRepository;
    private final SubcategoryRepository subcategoryRepository;
    private final ResumeMediaRepository resumeMediaRepository;
    private final MinioStorageService minioStorageService;
    private final FreeAccessTrackingRepository freeAccessTrackingRepository;
    private final ResumeNotificationService resumeNotificationService;
    @Autowired
    @Qualifier("mediaUploadExecutor")
    private Executor mediaUploadExecutor;

    private static final int MAX_PHOTOS = 10;
    private static final int MAX_VIDEOS = 3;
    private static final long MAX_PHOTO_SIZE = 10 * 1024 * 1024; // 10 MB
    private static final long MAX_VIDEO_SIZE = 100 * 1024 * 1024; // 100 MB

    @Transactional(readOnly = true)
    public ResumeStatsResponse getUserResumeStats(Long telegramId) {
        User user = userRepository.findByTelegramId(telegramId)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));

        List<Resume> resumes = resumeRepository.findByUser(user);

        long totalCount = resumes.size();
        long activeCount = resumes.stream()
                .filter(Resume::getIsActive)
                .count();
        long inactiveCount = totalCount - activeCount;

        ResumeStatsResponse response = new ResumeStatsResponse();
        response.setTotalCount(totalCount);
        response.setActiveCount(activeCount);
        response.setInactiveCount(inactiveCount);

        return response;
    }

    @Transactional
    public Resume createResume(Long telegramId, CreateResumeRequest request) {
        User user = userRepository.findByTelegramId(telegramId)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));

        City city = cityRepository.findById(request.getCityId())
                .orElseThrow(() -> new ResourceNotFoundException("Город не найден"));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Категория не найдена"));

        Subcategory subcategory = subcategoryRepository.findById(request.getSubcategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Подкатегория не найдена"));

        Resume resume = new Resume();
        resume.setUser(user);
        resume.setName(request.getName());
        resume.setAge(request.getAge());
        resume.setGender(request.getGender());
        resume.setCity(city);
        resume.setCategory(category);
        resume.setSubcategory(subcategory);
        resume.setExperience(request.getExperience());
        resume.setDescription(request.getDescription());
        resume.setIsActive(request.getIsActive());
        resume.setPhone(request.getPhone());

        resume = resumeRepository.save(resume);
        resumeNotificationService.notifyEmployersAboutNewResume(resume);
        return resume;
    }

    @Transactional(readOnly = true)
    public List<ResumeResponse> getUserResumes(Long telegramId) {
        User user = userRepository.findByTelegramId(telegramId)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));

        return resumeRepository.findByUser(user).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public Resume updateResumeStatus(Long resumeId, Long telegramId, Boolean isActive) {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new ResourceNotFoundException("Резюме не найдено"));

        if (!resume.getUser().getTelegramId().equals(telegramId)) {
            throw new AccessDeniedException("Доступ запрещён");
        }

        resume.setIsActive(isActive);
        return resumeRepository.save(resume);
    }

    @Transactional
    public void deleteResume(Long resumeId, Long telegramId) {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new ResourceNotFoundException("Резюме не найдено"));

        if (!resume.getUser().getTelegramId().equals(telegramId)) {
            throw new AccessDeniedException("Доступ запрещён");
        }

        // Удаляем все медиа файлы
        List<ResumeMedia> mediaList = resumeMediaRepository.findByResumeIdOrderByDisplayOrderAsc(resumeId);
        for (ResumeMedia media : mediaList) {
            try {
                String objectName = minioStorageService.extractObjectNameFromUrl(media.getFileUrl());
                String bucket = minioStorageService.extractBucketFromUrl(media.getFileUrl());
                if (objectName != null && bucket != null) {
                    minioStorageService.deleteFile(bucket, objectName);
                }
            } catch (Exception e) {
                // Логируем ошибку, но продолжаем удаление
            }
        }
        freeAccessTrackingRepository.deleteByResumeId(resumeId);


        resumeRepository.delete(resume);
    }

    @Transactional
    public Resume updateResume(Long resumeId, Long telegramId, CreateResumeRequest request) {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new ResourceNotFoundException("Резюме не найдено"));

        if (!resume.getUser().getTelegramId().equals(telegramId)) {
            throw new AccessDeniedException("Доступ запрещён");
        }

        if (request.getCityId() != null && !resume.getCity().getId().equals(request.getCityId())) {
            City city = cityRepository.findById(request.getCityId())
                    .orElseThrow(() -> new ResourceNotFoundException("Город не найден"));
            resume.setCity(city);
        }

        if (request.getCategoryId() != null && !resume.getCategory().getId().equals(request.getCategoryId())) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Категория не найдена"));
            resume.setCategory(category);
        }

        if (request.getSubcategoryId() != null && !resume.getSubcategory().getId().equals(request.getSubcategoryId())) {
            Subcategory subcategory = subcategoryRepository.findById(request.getSubcategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Подкатегория не найдена"));
            resume.setSubcategory(subcategory);
        }

        if (request.getName() != null) {
            resume.setName(request.getName());
        }
        if (request.getAge() != null) {
            resume.setAge(request.getAge());
        }
        if (request.getGender() != null) {
            resume.setGender(request.getGender());
        }
        if (request.getExperience() != null) {
            resume.setExperience(request.getExperience());
        }
        if (request.getDescription() != null) {
            resume.setDescription(request.getDescription());
        }
        if (request.getIsActive() != null) {
            resume.setIsActive(request.getIsActive());
        }
        if (request.getPhone() != null) {
            resume.setPhone(request.getPhone());
        }


        return resumeRepository.save(resume);
    }

    @Async("mediaUploadExecutor")
    public void addResumeMediaBatchAsync(Long resumeId, Long telegramId, List<BotVacancyController.FileData> files) {
        try {
            Resume resume = validateResumeOwnership(resumeId, telegramId);

            List<ResumeMedia> existingMedia = resumeMediaRepository
                    .findByResumeIdOrderByDisplayOrderAsc(resumeId);
            long currentPhotoCount = existingMedia.stream()
                    .filter(m -> m.getMediaType() == ResumeMedia.MediaType.PHOTO).count();
            long currentVideoCount = existingMedia.stream()
                    .filter(m -> m.getMediaType() == ResumeMedia.MediaType.VIDEO).count();
            int baseOrder = existingMedia.stream()
                    .mapToInt(ResumeMedia::getDisplayOrder).max().orElse(0);

            // Валидация
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
            List<CompletableFuture<BotVacancyService.UploadResult>> futures = new ArrayList<>();
            for (int i = 0; i < files.size(); i++) {
                final BotVacancyController.FileData fd = files.get(i);
                final int order = baseOrder + i + 1;
                futures.add(CompletableFuture.supplyAsync(() -> {
                    try {
                        ByteArrayMultipartFile mockFile =
                                new ByteArrayMultipartFile(fd.bytes(), fd.fileName(), fd.contentType());
                        String fileUrl = minioStorageService.uploadResumeFile(mockFile, resumeId);
                        return new BotVacancyService.UploadResult(fd, fileUrl, order);
                    } catch (Exception e) {
                        throw new RuntimeException("Ошибка загрузки: " + fd.fileName(), e);
                    }
                }, mediaUploadExecutor));
            }

            List<ResumeMedia> mediaList = futures.stream()
                    .map(f -> {
                        try { return f.get(60, TimeUnit.SECONDS); }
                        catch (Exception e) { throw new RuntimeException(e); }
                    })
                    .map(result -> ResumeMedia.builder()
                            .resume(resume)
                            .mediaType(result.fileData().contentType().startsWith("video/")
                                    ? ResumeMedia.MediaType.VIDEO
                                    : ResumeMedia.MediaType.PHOTO)
                            .fileUrl(result.fileUrl())
                            .fileName(result.fileData().fileName())
                            .fileSize(result.fileData().size())
                            .displayOrder(result.order())
                            .build())
                    .collect(Collectors.toList());

            resumeMediaRepository.saveAll(mediaList);
            log.info("Batch завершён: {} файлов для резюме {}", files.size(), resumeId);

        } catch (Exception e) {
            log.error("Ошибка batch загрузки для резюме {}", resumeId, e);
        }
    }
    /**
     * Получение всех медиа файлов резюме
     */
    @Transactional(readOnly = true)
    public List<MediaResponse> getResumeMedia(Long resumeId) {
        return resumeMediaRepository.findByResumeIdOrderByDisplayOrderAsc(resumeId)
                .stream()
                .map(this::mapMediaToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Удаление медиа файла
     */
    @Transactional
    public void deleteResumeMedia(Long mediaId, Long telegramId) throws Exception {
        ResumeMedia media = resumeMediaRepository.findById(mediaId)
                .orElseThrow(() -> new ResourceNotFoundException("Медиафайл не найден"));

        validateResumeOwnership(media.getResume().getId(), telegramId);

        // Удаление из MinIO
        String objectName = minioStorageService.extractObjectNameFromUrl(media.getFileUrl());
        String bucket = minioStorageService.extractBucketFromUrl(media.getFileUrl());

        if (objectName != null && bucket != null) {
            minioStorageService.deleteFile(bucket, objectName);
        }

        // Удаление из БД
        resumeMediaRepository.delete(media);
    }

    /**
     * Изменение порядка отображения медиа
     */
    @Transactional
    public void updateMediaOrder(Long mediaId, Long telegramId, Integer newOrder) {
        ResumeMedia media = resumeMediaRepository.findById(mediaId)
                .orElseThrow(() -> new ResourceNotFoundException("Медиафайл не найден"));

        validateResumeOwnership(media.getResume().getId(), telegramId);

        media.setDisplayOrder(newOrder);
        resumeMediaRepository.save(media);
    }

    private Resume validateResumeOwnership(Long resumeId, Long telegramId) {
        Resume resume = resumeRepository.findByIdWithUser(resumeId)
                .orElseThrow(() -> new ResourceNotFoundException("Резюме не найдено"));

        if (!resume.getUser().getTelegramId().equals(telegramId)) {
            throw new AccessDeniedException("Доступ запрещён");
        }

        return resume;
    }

    private ResumeResponse mapToResponse(Resume resume) {
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
        response.setIsActive(resume.getIsActive());
        response.setCreatedAt(resume.getCreatedAt());
        response.setPhone(response.getPhone());

        // Добавляем медиа файлы
        List<MediaResponse> mediaList = resumeMediaRepository
                .findByResumeIdOrderByDisplayOrderAsc(resume.getId())
                .stream()
                .map(this::mapMediaToResponse)
                .collect(Collectors.toList());
        response.setMedia(mediaList);

        return response;
    }

    private MediaResponse mapMediaToResponse(ResumeMedia media) {
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
}
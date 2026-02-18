package job.search.kg.service.user;

import job.search.kg.dto.request.user.CreateResumeRequest;
import job.search.kg.dto.response.MediaResponse;
import job.search.kg.dto.response.user.ResumeResponse;
import job.search.kg.dto.response.user.ResumeStatsResponse;
import job.search.kg.entity.*;
import job.search.kg.exceptions.ResourceNotFoundException;
import job.search.kg.repo.*;
import job.search.kg.service.MinioStorageService;
import job.search.kg.telegram.notification.ResumeNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
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
    public Resume createResume(Long telegramId, CreateResumeRequest request, MultipartFile photo) {
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

        if (photo != null && !photo.isEmpty()) {
            try {
                String fileUrl = minioStorageService.uploadResumeFile(photo, null);
                resume.setProfilePhotoUrl(fileUrl);
            } catch (Exception e) {
                log.warn("Failed to upload profile photo during resume creation: {}", e.getMessage());
            }
        }

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

    /**
     * Добавление фото к резюме
     */
    @Transactional
    public MediaResponse addResumePhoto(Long resumeId, Long telegramId, MultipartFile file) throws Exception {
        Resume resume = validateResumeOwnership(resumeId, telegramId);

        // Проверка лимита фото
        long photoCount = resumeMediaRepository.findByResumeIdOrderByDisplayOrderAsc(resumeId)
                .stream()
                .filter(m -> m.getMediaType() == ResumeMedia.MediaType.PHOTO)
                .count();

        if (photoCount >= MAX_PHOTOS) {
            throw new IllegalStateException("Достигнут максимальный лимит фотографий (" + MAX_PHOTOS + ")");
        }

        // Проверка размера файла
        if (file.getSize() > MAX_PHOTO_SIZE) {
            throw new IllegalArgumentException("Размер фото превышает максимально допустимый (10 МБ)");
        }

        // Проверка типа файла
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Недопустимый тип файла. Разрешены только изображения");
        }

        // Загрузка в MinIO
        String fileUrl = minioStorageService.uploadResumeFile(file, resumeId);

        // Сохранение в БД
        int nextOrder = resumeMediaRepository.findByResumeIdOrderByDisplayOrderAsc(resumeId)
                .stream()
                .mapToInt(ResumeMedia::getDisplayOrder)
                .max()
                .orElse(0) + 1;

        ResumeMedia media = ResumeMedia.builder()
                .resume(resume)
                .mediaType(ResumeMedia.MediaType.PHOTO)
                .fileUrl(fileUrl)
                .fileName(file.getOriginalFilename())
                .fileSize(file.getSize())
                .displayOrder(nextOrder)
                .build();

        media = resumeMediaRepository.save(media);

        return mapMediaToResponse(media);
    }

    /**
     * Добавление видео к резюме
     */
    @Transactional
    public MediaResponse addResumeVideo(Long resumeId, Long telegramId, MultipartFile file) throws Exception {
        Resume resume = validateResumeOwnership(resumeId, telegramId);

        // Проверка лимита видео
        long videoCount = resumeMediaRepository.findByResumeIdOrderByDisplayOrderAsc(resumeId)
                .stream()
                .filter(m -> m.getMediaType() == ResumeMedia.MediaType.VIDEO)
                .count();

        if (videoCount >= MAX_VIDEOS) {
            throw new IllegalStateException("Достигнут максимальный лимит видео (" + MAX_VIDEOS + ")");
        }

        // Проверка размера файла
        if (file.getSize() > MAX_VIDEO_SIZE) {
            throw new IllegalArgumentException("Размер видео превышает максимально допустимый (100 МБ)");
        }

        // Проверка типа файла
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("video/")) {
            throw new IllegalArgumentException("Недопустимый тип файла. Разрешены только видео");
        }

        // Загрузка в MinIO
        String fileUrl = minioStorageService.uploadResumeFile(file, resumeId);

        // Сохранение в БД
        int nextOrder = resumeMediaRepository.findByResumeIdOrderByDisplayOrderAsc(resumeId)
                .stream()
                .mapToInt(ResumeMedia::getDisplayOrder)
                .max()
                .orElse(0) + 1;

        ResumeMedia media = ResumeMedia.builder()
                .resume(resume)
                .mediaType(ResumeMedia.MediaType.VIDEO)
                .fileUrl(fileUrl)
                .fileName(file.getOriginalFilename())
                .fileSize(file.getSize())
                .displayOrder(nextOrder)
                .build();

        media = resumeMediaRepository.save(media);

        return mapMediaToResponse(media);
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
        Resume resume = resumeRepository.findById(resumeId)
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
                .fileUrl(media.getFileUrl())
                .fileName(media.getFileName())
                .fileSize(media.getFileSize())
                .displayOrder(media.getDisplayOrder())
                .uploadedAt(media.getUploadedAt())
                .build();
    }
}
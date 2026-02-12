package job.search.kg.service;

import io.minio.*;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class MinioStorageService {

    private final MinioClient minioClient;

    @Value("${minio.bucket.resumes}")
    private String resumesBucket;

    @Value("${minio.bucket.vacancies}")
    private String vacanciesBucket;

    @Value("${minio.url}")
    private String minioUrl;

    /**
     * Загрузка файла резюме
     */
    public String uploadResumeFile(MultipartFile file, Long resumeId) throws Exception {
        String fileName = generateFileName(file.getOriginalFilename(), resumeId);
        return uploadFile(file, resumesBucket, fileName);
    }

    /**
     * Загрузка файла вакансии
     */
    public String uploadVacancyFile(MultipartFile file, Long vacancyId) throws Exception {
        String fileName = generateFileName(file.getOriginalFilename(), vacancyId);
        return uploadFile(file, vacanciesBucket, fileName);
    }

    /**
     * Удаление файла
     */
    public void deleteFile(String bucketName, String objectName) throws Exception {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );
            log.info("🗑️ File deleted successfully: {}/{}", bucketName, objectName);
        } catch (Exception e) {
            log.error("❌ Error deleting file: {}/{}", bucketName, objectName, e);
            throw e;
        }
    }

    /**
     * Получение временной ссылки на файл (presigned URL)
     * Это ПУБЛИЧНАЯ ссылка, которая работает 7 дней
     */
    public String getPresignedUrl(String bucketName, String objectName) throws Exception {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucketName)
                            .object(objectName)
                            .expiry(7, TimeUnit.DAYS) // Ссылка действительна 7 дней
                            .build()
            );
        } catch (Exception e) {
            log.error("❌ Error generating presigned URL: {}/{}", bucketName, objectName, e);
            throw e;
        }
    }

    /**
     * Скачивание файла
     */
    public InputStream downloadFile(String bucketName, String objectName) throws Exception {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );
        } catch (Exception e) {
            log.error("❌ Error downloading file: {}/{}", bucketName, objectName, e);
            throw e;
        }
    }

    /**
     * Загрузка файла в MinIO/DigitalOcean Spaces
     */
    private String uploadFile(MultipartFile file, String bucketName, String fileName) throws Exception {
        try (InputStream inputStream = file.getInputStream()) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .stream(inputStream, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );

            log.info("✅ File uploaded successfully: {}/{}", bucketName, fileName);

            // Возвращаем URL файла
            return String.format("%s/%s/%s", minioUrl, bucketName, fileName);

        } catch (Exception e) {
            log.error("❌ Error uploading file: {}", fileName, e);
            throw new RuntimeException("Failed to upload file", e);
        }
    }

    /**
     * Генерация уникального имени файла
     * Формат: resumeId/uuid.extension
     */
    private String generateFileName(String originalFilename, Long entityId) {
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        return entityId + "/" + UUID.randomUUID() + extension;
    }

    /**
     * Извлечение имени объекта из URL
     * Пример URL: https://fra1.digitaloceanspaces.com/resumes/123/uuid.jpg
     * Возвращает: 123/uuid.jpg
     */
    public String extractObjectNameFromUrl(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return null;
        }

        try {
            // Убираем протокол и домен
            String[] parts = fileUrl.replace("https://", "").replace("http://", "").split("/");

            // parts[0] = домен (fra1.digitaloceanspaces.com)
            // parts[1] = bucket (resumes или vacancies)
            // parts[2+] = путь к файлу (entityId/filename)

            if (parts.length < 3) {
                return null;
            }

            // Собираем путь: entityId/filename
            StringBuilder objectName = new StringBuilder();
            for (int i = 2; i < parts.length; i++) {
                if (i > 2) objectName.append("/");
                objectName.append(parts[i]);
            }

            return objectName.toString();
        } catch (Exception e) {
            log.error("❌ Error extracting object name from URL: {}", fileUrl, e);
            return null;
        }
    }

    /**
     * Извлечение имени bucket из URL
     * Пример URL: https://fra1.digitaloceanspaces.com/resumes/123/uuid.jpg
     * Возвращает: resumes
     */
    public String extractBucketFromUrl(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return null;
        }

        if (fileUrl.contains("/" + resumesBucket + "/")) {
            return resumesBucket;
        } else if (fileUrl.contains("/" + vacanciesBucket + "/")) {
            return vacanciesBucket;
        }

        return null;
    }
}
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

    @Value("${minio.bucket}")
    private String bucket;
    @Value("${minio.url}")
    private String minioUrl;

    /**
     * Загрузка файла резюме
     */
    public String uploadResumeFile(MultipartFile file, Long resumeId) throws Exception {
        String fileName = generateFileName(file.getOriginalFilename(), resumeId);
        return uploadFile(file, fileName);
    }

    /**
     * Загрузка файла вакансии
     */
    public String uploadVacancyFile(MultipartFile file, Long vacancyId) throws Exception {
        String fileName = generateFileName(file.getOriginalFilename(), vacancyId);
        return uploadFile(file, fileName);
    }

    /**
     * Разрешает значение в presigned URL.
     * Если значение — полный URL (legacy) — извлекает objectKey и генерирует presigned.
     * Если значение — objectKey — сразу генерирует presigned URL.
     */
    public String resolveUrl(String value) {
        if (value == null || value.isBlank()) return null;
        if (value.startsWith("http")) {
            // Legacy: полный URL — извлекаем objectKey
            String marker = "/" + bucket + "/";
            int idx = value.indexOf(marker);
            if (idx >= 0) {
                String objectKey = value.substring(idx + marker.length());
                int qIdx = objectKey.indexOf('?');
                if (qIdx >= 0) objectKey = objectKey.substring(0, qIdx);
                return getPresignedUrl(objectKey);
            }
            return value; // внешний URL, возвращаем как есть
        }
        return getPresignedUrl(value);
    }

    /**
     * Генерирует временную presigned ссылку (действует 1 час)
     */
    public String getPresignedUrl(String objectKey) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .bucket(bucket)
                            .object(objectKey)
                            .method(Method.GET)
                            .expiry(1, TimeUnit.HOURS)
                            .build()
            );
        } catch (Exception e) {
            log.error("❌ Error generating presigned URL: {}/{}", bucket, objectKey, e);
            throw new RuntimeException("Could not generate file URL");
        }
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
     * Загрузка файла в хранилище
     */
    private String uploadFile(MultipartFile file, String fileName) throws Exception {
        try (InputStream inputStream = file.getInputStream()) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(fileName)
                            .stream(inputStream, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );

            log.info("✅ File uploaded successfully: {}/{}", bucket, fileName);

            return String.format("%s/%s/%s", minioUrl, bucket, fileName);

        } catch (Exception e) {
            log.error("❌ Error uploading file: {}", fileName, e);
            throw new RuntimeException("Failed to upload file", e);
        }
    }

    /**
     * Генерация уникального имени файла
     * Формат: entityId/uuid.extension
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
     * Пример URL: https://t3.storageapi.dev/bucket/123/uuid.jpg
     * Возвращает: 123/uuid.jpg
     */
    public String extractObjectNameFromUrl(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return null;
        }

        try {
            String[] parts = fileUrl.replace("https://", "").replace("http://", "").split("/");

            if (parts.length < 3) {
                return null;
            }

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
     */
    public String extractBucketFromUrl(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return null;
        }

        if (fileUrl.contains("/" + bucket + "/")) {
            return bucket;
        }

        return null;
    }
}
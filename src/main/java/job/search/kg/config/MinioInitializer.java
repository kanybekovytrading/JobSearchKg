package job.search.kg.config;

import io.minio.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MinioInitializer implements CommandLineRunner {

    private final MinioClient minioClient;

    @Value("${minio.bucket}")
    private String resumesBucket;

    @Override
    public void run(String... args) throws Exception {
        createBucketIfNotExists(resumesBucket);
        // Устанавливаем публичную политику для обоих бакетов
        setPublicPolicy(resumesBucket);
    }

    private void createBucketIfNotExists(String bucketName) {
        try {
            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(bucketName).build()
            );

            if (!exists) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder().bucket(bucketName).build()
                );
                log.info("✅ Created bucket '{}'", bucketName);
            } else {
                log.info("✅ Bucket '{}' already exists", bucketName);
            }
        } catch (io.minio.errors.ErrorResponseException e) {
            // On managed S3 providers (e.g. Tigris), buckets are pre-provisioned and
            // bucket-management API calls return 403. Assume bucket exists and continue.
            if ("AccessDenied".equals(e.errorResponse().code()) || e.response().code() == 403) {
                log.warn("⚠️ Cannot check/create bucket '{}' (Access Denied) — assuming it exists", bucketName);
            } else {
                log.error("❌ Error creating bucket '{}'", bucketName, e);
                throw new RuntimeException(e);
            }
        } catch (Exception e) {
            log.error("❌ Error creating bucket '{}'", bucketName, e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Устанавливает публичную политику чтения для bucket
     * Это позволяет всем читать файлы без авторизации
     */
    private void setPublicPolicy(String bucketName) {
        try {
            String policy = """
                {
                  "Version": "2012-10-17",
                  "Statement": [
                    {
                      "Effect": "Allow",
                      "Principal": {"AWS": ["*"]},
                      "Action": ["s3:GetObject"],
                      "Resource": ["arn:aws:s3:::%s/*"]
                    }
                  ]
                }
                """.formatted(bucketName);

            minioClient.setBucketPolicy(
                    SetBucketPolicyArgs.builder()
                            .bucket(bucketName)
                            .config(policy)
                            .build()
            );
        } catch (Exception e) {
            log.error("❌ Error setting public policy for bucket '{}'", bucketName, e);
            // Не бросаем исключение, так как bucket уже может иметь политику
        }
    }
}
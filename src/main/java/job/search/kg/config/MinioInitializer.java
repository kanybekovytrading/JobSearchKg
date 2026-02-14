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

    @Value("${minio.bucket.resumes}")
    private String resumesBucket;

    @Value("${minio.bucket.vacancies}")
    private String vacanciesBucket;

    @Override
    public void run(String... args) throws Exception {
        createBucketIfNotExists(resumesBucket);
        createBucketIfNotExists(vacanciesBucket);

        // Устанавливаем публичную политику для обоих бакетов
        setPublicPolicy(resumesBucket);
        setPublicPolicy(vacanciesBucket);
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
package job.search.kg;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class JobSearchKgApplication {

    public static void main(String[] args) {
        SpringApplication.run(JobSearchKgApplication.class, args);
    }

}

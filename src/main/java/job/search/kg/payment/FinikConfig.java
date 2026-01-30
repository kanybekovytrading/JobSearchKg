package job.search.kg.payment;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "finik")
@Data
public class FinikConfig {

    private String apiKey;
    private String accountId;
    private String baseUrl;
    private String privateKeyPath;
    private String webhookURL;

}
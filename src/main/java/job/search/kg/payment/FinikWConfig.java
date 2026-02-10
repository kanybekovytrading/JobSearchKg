package job.search.kg.payment;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "finik-withdraw")
@Data
public class FinikWConfig {

    private String apiKey;
    private String accountId;
    private String baseUrl;
    private String privateKeyPath;
    private String publicKeyPath;
    private String webhookURL;
    private String userId;

}
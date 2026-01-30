package job.search.kg.dto.response.user;


import lombok.AllArgsConstructor;
import lombok.Data;


@Data
@AllArgsConstructor
public class CreatePaymentResponse {
    private String paymentId;
    private String paymentUrl;
    private String status;
}
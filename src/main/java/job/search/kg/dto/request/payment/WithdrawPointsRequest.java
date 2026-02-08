package job.search.kg.dto.request.payment;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
@Schema(description = "Запрос на вывод баллов в деньги")
public class WithdrawPointsRequest {

    @NotNull(message = "Points amount is required")
    @Schema(description = "Количество баллов для вывода", example = "5000")
    private Integer pointsAmount;

    @NotBlank(message = "Service ID is required")
    @Schema(description = "ID сервиса (например, megacom, beeline)", example = "megacom")
    private String serviceId;

    @NotBlank(message = "Recipient phone is required")
    @Pattern(regexp = "^\\+996\\d{9}$", message = "Phone must be in format +996XXXXXXXXX")
    @Schema(description = "Номер телефона получателя", example = "+996700123456")
    private String recipientPhone;
}

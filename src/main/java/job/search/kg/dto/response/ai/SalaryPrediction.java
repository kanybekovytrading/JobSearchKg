package job.search.kg.dto.response.ai;

import lombok.Data;

@Data
public class SalaryPrediction {
    private Double averageSalary;
    private String explanation;
}

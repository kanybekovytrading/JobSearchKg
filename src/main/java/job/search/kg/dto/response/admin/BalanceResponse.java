package job.search.kg.dto.response.admin;
import job.search.kg.entity.PointsTransaction;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BalanceResponse {
    private Integer balance;
    private List<PointsTransaction> transactions;
}
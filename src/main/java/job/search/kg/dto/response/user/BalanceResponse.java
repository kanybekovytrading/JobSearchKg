package job.search.kg.dto.response.user;
import job.search.kg.entity.PointsTransaction;
import lombok.Data;

import java.util.List;

@Data
public class BalanceResponse {
    private Integer balance;
    private List<PointsTransaction> transactions;
}

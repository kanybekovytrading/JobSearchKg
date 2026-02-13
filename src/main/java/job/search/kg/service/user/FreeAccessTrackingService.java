package job.search.kg.service.user;

import job.search.kg.entity.FreeAccessTracking;
import job.search.kg.repo.FreeAccessTrackingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FreeAccessTrackingService {

    private final FreeAccessTrackingRepository freeAccessTrackingRepository;

    @Transactional
    public void saveBatch(Long telegramId, String searchKey, List<Long> entityIds, LocalDate date) {
        List<FreeAccessTracking> trackings = entityIds.stream()
                .map(entityId -> FreeAccessTracking.builder()
                        .telegramId(telegramId)
                        .searchKey(searchKey)
                        .entityId(entityId)
                        .accessDate(date)
                        .build())
                .collect(Collectors.toList());

        freeAccessTrackingRepository.saveAll(trackings);
    }
}

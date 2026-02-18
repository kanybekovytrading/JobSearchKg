package job.search.kg.repo;

import job.search.kg.entity.FreeAccessTracking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface FreeAccessTrackingRepository extends JpaRepository<FreeAccessTracking, Long> {

    /**
     * Найти ID сущностей (резюме/вакансий) с бесплатным доступом для конкретного пользователя,
     * комбинации поиска и даты
     */
    @Query("SELECT fat.entityId FROM FreeAccessTracking fat " +
            "WHERE fat.telegramId = :telegramId " +
            "AND fat.searchKey = :searchKey " +
            "AND fat.accessDate = :date " +
            "ORDER BY fat.createdAt ASC")
    List<Long> findTodayFreeAccessIds(
            @Param("telegramId") Long telegramId,
            @Param("searchKey") String searchKey,
            @Param("date") LocalDate date
    );

    /**
     * Удалить старые записи (старше указанной даты)
     * Можно вызывать по расписанию для очистки базы
     */
    @Modifying
    @Query("DELETE FROM FreeAccessTracking fat WHERE fat.accessDate < :date")
    void deleteOldRecords(@Param("date") LocalDate date);

    @Modifying
    @Query("DELETE FROM FreeAccessTracking f WHERE f.entityId = :vacancyId AND f.searchKey LIKE 'VACANCY%'")
    void deleteByVacancyId(@Param("vacancyId") Long vacancyId);

    @Modifying
    @Query("DELETE FROM FreeAccessTracking f WHERE f.entityId = :resumeId AND f.searchKey LIKE 'RESUME%'")
    void deleteByResumeId(@Param("resumeId") Long resumeId);

    @Query("SELECT COUNT(fat) > 0 FROM FreeAccessTracking fat " +
            "WHERE fat.telegramId = :telegramId " +
            "AND fat.entityId = :entityId " +
            "AND fat.accessDate = :date " +
            "AND fat.searchKey LIKE :prefix%")
    boolean existsByTelegramIdAndEntityIdAndDate(
            @Param("telegramId") Long telegramId,
            @Param("entityId") Long entityId,
            @Param("date") LocalDate date,
            @Param("prefix") String prefix
    );

}
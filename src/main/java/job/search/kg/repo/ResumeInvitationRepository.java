package job.search.kg.repo;

import job.search.kg.entity.ResumeInvitation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResumeInvitationRepository extends JpaRepository<ResumeInvitation, Long> {
    boolean existsByResumeIdAndVacancyId(Long resumeId, Long vacancyId);

    List<ResumeInvitation> findByResumeIdOrderByCreatedAtDesc(Long resumeId);

}

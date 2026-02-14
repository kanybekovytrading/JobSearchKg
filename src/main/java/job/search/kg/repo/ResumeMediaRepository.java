package job.search.kg.repo;

import job.search.kg.entity.ResumeMedia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResumeMediaRepository extends JpaRepository<ResumeMedia, Long> {

    List<ResumeMedia> findByResumeIdOrderByDisplayOrderAsc(Long resumeId);

}
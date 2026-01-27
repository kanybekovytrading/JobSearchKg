package job.search.kg.service.admin;

import job.search.kg.entity.Resume;
import job.search.kg.exceptions.ResourceNotFoundException;
import job.search.kg.repo.ResumeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminResumeService {

    private final ResumeRepository resumeRepository;

    @Transactional(readOnly = true)
    public Page<Resume> getAllResumes(Pageable pageable) {
        return resumeRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Resume getResumeById(Long id) {
        return resumeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found"));
    }

    @Transactional
    public void deleteResume(Long id) {
        Resume resume = getResumeById(id);
        resumeRepository.delete(resume);
    }

    @Transactional(readOnly = true)
    public Long countActiveResumes() {
        return resumeRepository.countByIsActive(true);
    }

    @Transactional(readOnly = true)
    public Long countInactiveResumes() {
        return resumeRepository.countByIsActive(false);
    }
}

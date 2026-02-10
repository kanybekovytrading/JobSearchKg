package job.search.kg.controller.admin;

import job.search.kg.dto.response.admin.ResumeResponse;
import job.search.kg.entity.Resume;
import job.search.kg.mapper.ResumeMapper;
import job.search.kg.service.admin.AdminResumeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/resumes")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminResumeController {

    private final AdminResumeService adminResumeService;
    private final ResumeMapper resumeMapper;

    @GetMapping
    public ResponseEntity<Page<ResumeResponse>> getAllResumes(Pageable pageable) {
        Page<Resume> resumes = adminResumeService.getAllResumes(pageable);
        Page<ResumeResponse> response = resumes.map(resumeMapper::toResponse);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResumeResponse> getResumeById(@PathVariable Long id) {
        Resume resume = adminResumeService.getResumeById(id);
        ResumeResponse response = resumeMapper.toResponse(resume);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteResume(@PathVariable Long id) {
        adminResumeService.deleteResume(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/stats/active-count")
    public ResponseEntity<Long> countActiveResumes() {
        Long count = adminResumeService.countActiveResumes();
        return ResponseEntity.ok(count);
    }
}

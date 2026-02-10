package job.search.kg.dto.response.ai;

import lombok.Data;

import java.util.List;

@Data
public class ResumeAnalysis {
    private Integer score;
    private String summary;
    private List<String> strengths;
    private List<String> weaknesses;
}


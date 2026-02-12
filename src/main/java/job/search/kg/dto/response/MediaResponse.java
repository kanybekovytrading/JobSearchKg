package job.search.kg.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MediaResponse {
    private Long id;
    private String mediaType; // PHOTO or VIDEO
    private String fileUrl;
    private String fileName;
    private Long fileSize;
    private Integer displayOrder;
    private LocalDateTime uploadedAt;
}
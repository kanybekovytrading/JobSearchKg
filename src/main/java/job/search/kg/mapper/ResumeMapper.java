package job.search.kg.mapper;

import job.search.kg.dto.response.admin.*;
import job.search.kg.entity.Resume;
import org.springframework.stereotype.Component;

@Component
public class ResumeMapper {

    public ResumeResponse toResponse(Resume resume) {
        ResumeResponse response = new ResumeResponse();
        response.setId(resume.getId());
        response.setName(resume.getName());
        response.setAge(resume.getAge());
        response.setGender(resume.getGender() != null ? resume.getGender().name() : null);
        response.setExperience(resume.getExperience());
        response.setDescription(resume.getDescription());
        response.setIsActive(resume.getIsActive());
        response.setCreatedAt(resume.getCreatedAt());
        response.setUpdatedAt(resume.getUpdatedAt());

        // City
        if (resume.getCity() != null) {
            SimpleResponse cityResponse = new SimpleResponse();
            cityResponse.setId(resume.getCity().getId());
            cityResponse.setName(resume.getCity().getNameRu());
            response.setCity(cityResponse);
        }

        // Category
        if (resume.getCategory() != null) {
            SimpleResponse categoryResponse = new SimpleResponse();
            categoryResponse.setId(resume.getCategory().getId());
            categoryResponse.setName(resume.getCategory().getNameRu());
            response.setCategory(categoryResponse);
        }

        // Subcategory
        if (resume.getSubcategory() != null) {
            SimpleResponse subcategoryResponse = new SimpleResponse();
            subcategoryResponse.setId(resume.getSubcategory().getId());
            subcategoryResponse.setName(resume.getSubcategory().getNameRu());
            response.setSubcategory(subcategoryResponse);
        }

        return response;
    }
}

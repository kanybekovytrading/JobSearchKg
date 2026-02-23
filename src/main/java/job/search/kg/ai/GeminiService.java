package job.search.kg.ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import job.search.kg.dto.response.ai.CoverLetter;
import job.search.kg.dto.response.ai.ResumeAnalysis;
import job.search.kg.dto.response.ai.SalaryPrediction;
import job.search.kg.entity.*;
import job.search.kg.exceptions.ResourceNotFoundException;
import job.search.kg.repo.ResumeRepository;
import job.search.kg.repo.UserRepository;
import job.search.kg.repo.VacancyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeminiService {
    private final UserRepository userRepository;
    private final ResumeRepository repository;
    private final VacancyRepository vacancyRepository;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;


    @Value("${claude.api-key}")
    private String apiKey;

    @Value("${claude.model}")
    private String model;

    private static final String CLAUDE_API_URL = "https://api.anthropic.com/v1/messages";
    private static final String ANTHROPIC_VERSION = "2023-06-01";

    public ResumeAnalysis analyzeResume(Long resumeId, Long telegramId) {
        User user = userRepository.findByTelegramId(telegramId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Resume resume = repository.findById(resumeId)
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found"));

        User.Language userLanguage = user.getLanguage();
        String categoryName = getCategoryName(resume.getCategory(), userLanguage);
        String subcategoryName = getSubcategoryName(resume.getSubcategory(), userLanguage);

        String prompt = buildAnalyzeResumePrompt(resume, categoryName, subcategoryName, userLanguage);

        return callClaude(prompt, ResumeAnalysis.class);
    }

    public SalaryPrediction predictSalary(Long vacancyId, Long telegramId) {
        User user = userRepository.findByTelegramId(telegramId).orElseThrow(
                () -> new ResourceNotFoundException("User not found")
        );

        Vacancy vacancy = vacancyRepository.findById(vacancyId).orElseThrow(
                () -> new ResourceNotFoundException("Vacancy not found")
        );

        User.Language userLanguage = user.getLanguage();
        String categoryName = getCategoryName(vacancy.getCategory(), userLanguage);
        String subcategoryName = getSubcategoryName(vacancy.getSubcategory(), userLanguage);

        String prompt = buildPredictSalaryPrompt(vacancy, categoryName, subcategoryName, userLanguage);

        return callClaude(prompt, SalaryPrediction.class);
    }

    public CoverLetter generateCoverLetter(Long resumeId, Long vacancyId, Long telegramId) {
        User user = userRepository.findByTelegramId(telegramId).orElseThrow(
                () -> new ResourceNotFoundException("User not found")
        );

        Resume resume = repository.findById(resumeId).orElseThrow(
                () -> new ResourceNotFoundException("Resume not found")
        );

        Vacancy vacancy = vacancyRepository.findById(vacancyId).orElseThrow(
                () -> new ResourceNotFoundException("Vacancy not found")
        );

        User.Language userLanguage = user.getLanguage();

        String categoryName = getCategoryName(resume.getCategory(), userLanguage);
        String subcategoryName = getSubcategoryName(resume.getSubcategory(), userLanguage);

        String prompt = buildPrompt(resume, vacancy, categoryName, subcategoryName, userLanguage);

        return callClaude(prompt, CoverLetter.class);
    }

// ==================== ANALYZE RESUME PROMPTS ====================

    private String buildAnalyzeResumePrompt(Resume resume, String categoryName,
                                            String subcategoryName, User.Language language) {
        return switch (language) {
            case EN -> buildAnalyzeResumeEnglish(resume, categoryName, subcategoryName);
            case KY -> buildAnalyzeResumeKyrgyz(resume, categoryName, subcategoryName);
            default -> buildAnalyzeResumeRussian(resume, categoryName, subcategoryName);
        };
    }

    private String buildAnalyzeResumeRussian(Resume resume, String categoryName, String subcategoryName) {
        String resumeData = String.format(
                "Имя: %s, Возраст: %d, Опыт: %d лет, Категория: %s (%s), Описание: %s",
                resume.getName(),
                resume.getAge() != null ? resume.getAge() : 0,
                resume.getExperience() != null ? resume.getExperience() : 0,
                categoryName,
                subcategoryName,
                resume.getDescription() != null ? resume.getDescription() : ""
        );

        return String.format(
                "Проведи профессиональный анализ этого резюме для рынка Кыргызстана. " +
                        "Дай оценку от 0 до 100 и краткие советы по улучшению. ДАННЫЕ: %s\n\n" +
                        "Ответь ТОЛЬКО в формате JSON без дополнительного текста:\n" +
                        "{\n" +
                        "  \"score\": число от 0 до 100,\n" +
                        "  \"summary\": \"общий вывод на русском\",\n" +
                        "  \"strengths\": [\"сильная сторона 1\", \"сильная сторона 2\"],\n" +
                        "  \"weaknesses\": [\"слабая сторона 1\", \"слабая сторона 2\"]\n" +
                        "}",
                resumeData
        );
    }

    private String buildAnalyzeResumeEnglish(Resume resume, String categoryName, String subcategoryName) {
        String resumeData = String.format(
                "Name: %s, Age: %d, Experience: %d years, Category: %s (%s), Description: %s",
                resume.getName(),
                resume.getAge() != null ? resume.getAge() : 0,
                resume.getExperience() != null ? resume.getExperience() : 0,
                categoryName,
                subcategoryName,
                resume.getDescription() != null ? resume.getDescription() : ""
        );

        return String.format(
                "Conduct a professional analysis of this resume for the Kyrgyzstan job market. " +
                        "Give a score from 0 to 100 and brief improvement tips. DATA: %s\n\n" +
                        "Respond ONLY in JSON format without additional text:\n" +
                        "{\n" +
                        "  \"score\": number from 0 to 100,\n" +
                        "  \"summary\": \"overall conclusion in English\",\n" +
                        "  \"strengths\": [\"strength 1\", \"strength 2\"],\n" +
                        "  \"weaknesses\": [\"weakness 1\", \"weakness 2\"]\n" +
                        "}",
                resumeData
        );
    }

    private String buildAnalyzeResumeKyrgyz(Resume resume, String categoryName, String subcategoryName) {
        String resumeData = String.format(
                "Аты: %s, Жашы: %d, Тажрыйба: %d жыл, Категория: %s (%s), Сүрөттөмө: %s",
                resume.getName(),
                resume.getAge() != null ? resume.getAge() : 0,
                resume.getExperience() != null ? resume.getExperience() : 0,
                categoryName,
                subcategoryName,
                resume.getDescription() != null ? resume.getDescription() : ""
        );

        return String.format(
                "Кыргызстандын эмгек рыногу үчүн бул резюмени кесиптик талдаңыз. " +
                        "0дөн 100гө чейин баа бериңиз жана жакшыртуу боюнча кыскача кеңештерди бериңиз. МААЛЫМАТТАР: %s\n\n" +
                        "Жооп БОЙДОН JSON форматында кошумча текстсиз:\n" +
                        "{\n" +
                        "  \"score\": 0дөн 100гө чейин сан,\n" +
                        "  \"summary\": \"жалпы корутунду кыргызча\",\n" +
                        "  \"strengths\": [\"күчтүү жагы 1\", \"күчтүү жагы 2\"],\n" +
                        "  \"weaknesses\": [\"алсыз жагы 1\", \"алсыз жагы 2\"]\n" +
                        "}",
                resumeData
        );
    }

// ==================== PREDICT SALARY PROMPTS ====================

    private String buildPredictSalaryPrompt(Vacancy vacancy, String categoryName,
                                            String subcategoryName, User.Language language) {
        return switch (language) {
            case EN -> buildPredictSalaryEnglish(vacancy, categoryName, subcategoryName);
            case KY -> buildPredictSalaryKyrgyz(vacancy, categoryName, subcategoryName);
            default -> buildPredictSalaryRussian(vacancy, categoryName, subcategoryName);
        };
    }

    private String buildPredictSalaryRussian(Vacancy vacancy, String categoryName, String subcategoryName) {
        String vacancyData = String.format(
                "Должность: %s, Компания: %s, Категория: %s (%s), Требуемый опыт: %d лет, Описание: %s",
                vacancy.getTitle(),
                vacancy.getCompanyName() != null ? vacancy.getCompanyName() : "не указано",
                categoryName,
                subcategoryName,
                vacancy.getExperienceInYear() != null ? vacancy.getExperienceInYear() : 0,
                vacancy.getDescription() != null ? vacancy.getDescription() : ""
        );

        return String.format(
                "На основе опыта и сферы деятельности, предскажи реальную зарплату в сомах (KGS) " +
                        "для этой позиции в Кыргызстане. ДАННЫЕ: %s\n\n" +
                        "Ответь ТОЛЬКО в формате JSON без дополнительного текста:\n" +
                        "{\n" +
                        "  \"averageSalary\": число в сомах,\n" +
                        "  \"explanation\": \"почему такая сумма (на русском)\"\n" +
                        "}",
                vacancyData
        );
    }

    private String buildPredictSalaryEnglish(Vacancy vacancy, String categoryName, String subcategoryName) {
        String vacancyData = String.format(
                "Position: %s, Company: %s, Category: %s (%s), Required experience: %d years, Description: %s",
                vacancy.getTitle(),
                vacancy.getCompanyName() != null ? vacancy.getCompanyName() : "not specified",
                categoryName,
                subcategoryName,
                vacancy.getExperienceInYear() != null ? vacancy.getExperienceInYear() : 0,
                vacancy.getDescription() != null ? vacancy.getDescription() : ""
        );

        return String.format(
                "Based on experience and field of activity, predict the real salary in soms (KGS) " +
                        "for this position in Kyrgyzstan. DATA: %s\n\n" +
                        "Respond ONLY in JSON format without additional text:\n" +
                        "{\n" +
                        "  \"averageSalary\": number in soms,\n" +
                        "  \"explanation\": \"why this amount (in English)\"\n" +
                        "}",
                vacancyData
        );
    }

    private String buildPredictSalaryKyrgyz(Vacancy vacancy, String categoryName, String subcategoryName) {
        String vacancyData = String.format(
                "Кызмат орду: %s, Компания: %s, Категория: %s (%s), Талап кылынган тажрыйба: %d жыл, Сүрөттөмө: %s",
                vacancy.getTitle(),
                vacancy.getCompanyName() != null ? vacancy.getCompanyName() : "көрсөтүлгөн эмес",
                categoryName,
                subcategoryName,
                vacancy.getExperienceInYear() != null ? vacancy.getExperienceInYear() : 0,
                vacancy.getDescription() != null ? vacancy.getDescription() : ""
        );

        return String.format(
                "Тажрыйбага жана иш чөйрөсүнө жараша, Кыргызстандагы бул кызмат үчүн " +
                        "чыныгы айлык акыны сомдо (KGS) болжолдоңуз. МААЛЫМАТТАР: %s\n\n" +
                        "Жооп БОЙДОН JSON форматында кошумча текстсиз:\n" +
                        "{\n" +
                        "  \"averageSalary\": сомдогу сан,\n" +
                        "  \"explanation\": \"эмне үчүн ушул сумма (кыргызча)\"\n" +
                        "}",
                vacancyData
        );
    }

// ==================== COVER LETTER PROMPTS ====================

    private String buildPrompt(Resume resume, Vacancy vacancy, String categoryName,
                               String subcategoryName, User.Language language) {
        return switch (language) {
            case EN -> buildEnglishPrompt(resume, vacancy, categoryName, subcategoryName);
            case KY -> buildKyrgyzPrompt(resume, vacancy, categoryName, subcategoryName);
            default -> buildRussianPrompt(resume, vacancy, categoryName, subcategoryName);
        };
    }

    private String buildRussianPrompt(Resume resume, Vacancy vacancy, String categoryName, String subcategoryName) {
        String resumeData = String.format(
                "Имя: %s, Возраст: %d, Опыт: %d лет, Категория: %s (%s), Описание: %s",
                resume.getName(),
                resume.getAge() != null ? resume.getAge() : 0,
                resume.getExperience() != null ? resume.getExperience() : 0,
                categoryName,
                subcategoryName,
                resume.getDescription() != null ? resume.getDescription() : ""
        );

        String vacancyData = String.format(
                "Должность: %s, Компания: %s, Зарплата: %s, Требования: %s, Опыт: %d лет",
                vacancy.getTitle(),
                vacancy.getCompanyName() != null ? vacancy.getCompanyName() : "не указано",
                vacancy.getSalary() != null ? vacancy.getSalary() : "не указано",
                vacancy.getDescription() != null ? vacancy.getDescription() : "",
                vacancy.getExperienceInYear() != null ? vacancy.getExperienceInYear() : 0
        );

        return String.format(
                "Напиши короткий, убедительный отклик (сопроводительное письмо) на вакансию от имени кандидата. " +
                        "Стиль: профессиональный, вежливый. Язык: русский. " +
                        "ВАКАНСИЯ: %s " +
                        "КАНДИДАТ: %s\n\n" +
                        "Ответь ТОЛЬКО в формате JSON без дополнительного текста:\n" +
                        "{\n" +
                        "  \"letter\": \"текст сопроводительного письма\"\n" +
                        "}",
                vacancyData, resumeData
        );
    }

    private String buildEnglishPrompt(Resume resume, Vacancy vacancy, String categoryName, String subcategoryName) {
        String resumeData = String.format(
                "Name: %s, Age: %d, Experience: %d years, Category: %s (%s), Description: %s",
                resume.getName(),
                resume.getAge() != null ? resume.getAge() : 0,
                resume.getExperience() != null ? resume.getExperience() : 0,
                categoryName,
                subcategoryName,
                resume.getDescription() != null ? resume.getDescription() : ""
        );

        String vacancyData = String.format(
                "Position: %s, Company: %s, Salary: %s, Requirements: %s, Experience: %d years",
                vacancy.getTitle(),
                vacancy.getCompanyName() != null ? vacancy.getCompanyName() : "not specified",
                vacancy.getSalary() != null ? vacancy.getSalary() : "not specified",
                vacancy.getDescription() != null ? vacancy.getDescription() : "",
                vacancy.getExperienceInYear() != null ? vacancy.getExperienceInYear() : 0
        );

        return String.format(
                "Write a short, persuasive cover letter for a job application on behalf of the candidate. " +
                        "Style: professional, polite. Language: English. " +
                        "VACANCY: %s " +
                        "CANDIDATE: %s\n\n" +
                        "Respond ONLY in JSON format without additional text:\n" +
                        "{\n" +
                        "  \"letter\": \"cover letter text\"\n" +
                        "}",
                vacancyData, resumeData
        );
    }

    private String buildKyrgyzPrompt(Resume resume, Vacancy vacancy, String categoryName, String subcategoryName) {
        String resumeData = String.format(
                "Аты: %s, Жашы: %d, Тажрыйба: %d жыл, Категория: %s (%s), Сүрөттөмө: %s",
                resume.getName(),
                resume.getAge() != null ? resume.getAge() : 0,
                resume.getExperience() != null ? resume.getExperience() : 0,
                categoryName,
                subcategoryName,
                resume.getDescription() != null ? resume.getDescription() : ""
        );

        String vacancyData = String.format(
                "Кызмат орду: %s, Компания: %s, Айлык акы: %s, Талаптар: %s, Тажрыйба: %d жыл",
                vacancy.getTitle(),
                vacancy.getCompanyName() != null ? vacancy.getCompanyName() : "көрсөтүлгөн эмес",
                vacancy.getSalary() != null ? vacancy.getSalary() : "көрсөтүлгөн эмес",
                vacancy.getDescription() != null ? vacancy.getDescription() : "",
                vacancy.getExperienceInYear() != null ? vacancy.getExperienceInYear() : 0
        );

        return String.format(
                "Талапкердин атынан жумушка өтүнүч катын жазыңыз. " +
                        "Стили: кесиптик, сылык. Тил: кыргызча. " +
                        "ВАКАНСИЯ: %s " +
                        "ТАЛАПКЕР: %s\n\n" +
                        "Жооп БОЙДОН JSON форматында кошумча текстсиз:\n" +
                        "{\n" +
                        "  \"letter\": \"кат тексти\"\n" +
                        "}",
                vacancyData, resumeData
        );
    }

// ==================== HELPER METHODS ====================

    private String getCategoryName(Category category, User.Language language) {
        if (category == null) return getNotSpecifiedText(language);

        return switch (language) {
            case EN -> category.getNameEn() != null ? category.getNameEn() : category.getNameRu();
            case KY -> category.getNameKy() != null ? category.getNameKy() : category.getNameRu();
            default -> category.getNameRu();
        };
    }

    private String getSubcategoryName(Subcategory subcategory, User.Language language) {
        if (subcategory == null) return getNotSpecifiedText(language);

        return switch (language) {
            case EN -> subcategory.getNameEn() != null ? subcategory.getNameEn() : subcategory.getNameRu();
            case KY -> subcategory.getNameKy() != null ? subcategory.getNameKy() : subcategory.getNameRu();
            default -> subcategory.getNameRu();
        };
    }

    private String getNotSpecifiedText(User.Language language) {
        return switch (language) {
            case EN -> "not specified";
            case KY -> "көрсөтүлгөн эмес";
            default -> "не указано";
        };
    }


    private <T> T callClaude(String prompt, Class<T> responseType) {
        Map<String, Object> requestBody = Map.of(
                "model", model,
                "max_tokens", 2000,
                "messages", List.of(Map.of("role", "user", "content", prompt))
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-api-key", apiKey);
        headers.set("anthropic-version", ANTHROPIC_VERSION);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(
                    CLAUDE_API_URL, request, String.class  // String вместо JsonNode
            );

            String responseBody = response.getBody();
            JsonNode json = objectMapper.readTree(responseBody); // парсим через objectMapper

            if (json.has("error")) {
                throw new RuntimeException("Claude error: " + json.get("error"));
            }

            String text = json.path("content").get(0).path("text").asText();
            log.info("Raw response text: {}", text);

            String cleanJson = text
                    .replace("```json", "")
                    .replace("```", "")
                    .trim();

            return objectMapper.readValue(cleanJson, responseType);

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("❌ Claude API error: {} | body: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Claude API call failed", e);
        } catch (Exception e) {
            log.error("❌ Error: {}", e.getMessage(), e);
            throw new RuntimeException("Claude call failed", e);
        }
    }
}
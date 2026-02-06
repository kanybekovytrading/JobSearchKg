package job.search.kg.dto.response.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceDTO {

    /**
     * ID услуги (например: "averspay", "o-kg", "mega-kg")
     */
    private String id;

    /**
     * Страна
     */
    private String country;

    /**
     * Логотип услуги
     */
    private LogoInfo logo;

    /**
     * Минимальная сумма платежа
     */
    private Integer minAmount;

    /**
     * Максимальная сумма платежа
     */
    private Integer maxAmount;

    /**
     * Название на английском
     */
    private String name_en;

    /**
     * Название на кыргызском
     */
    private String name_ky;

    /**
     * Название на русском
     */
    private String name_ru;

    /**
     * ID родительской категории
     */
    private String parentId;

    /**
     * Рейтинг популярности
     */
    private Double popularityScore;

    /**
     * Обязательные поля для платежа
     */
    private List<RequiredField> requiredFields;

    /**
     * Статус услуги (ENABLED, DISABLED, INACTIVE)
     */
    private String status;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LogoInfo {
        private String url;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RequiredField {
        /**
         * ID поля (например: "phone", "amount", "account.value")
         */
        private String fieldId;

        /**
         * Маска ввода (например: "(000)-000-000")
         */
        private String inputMask;

        /**
         * Тип клавиатуры (PHONE, MONEY, TEXT)
         */
        private String keyboardType;

        /**
         * Название на английском
         */
        private String label_en;

        /**
         * Название на кыргызском
         */
        private String label_ky;

        /**
         * Название на русском
         */
        private String label_ru;

        /**
         * Максимальная длина
         */
        private Integer maxLength;

        /**
         * Минимальная длина
         */
        private Integer minLength;

        /**
         * Префикс (например: "+996")
         */
        private String prefix;

        /**
         * Значение по умолчанию
         */
        private String value;
    }
}

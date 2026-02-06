package job.search.kg.dto.request.payment;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder(alphabetic = true)
public class GetServicesRequest {

    /**
     * Начальная позиция (по умолчанию 0)
     */
    private Integer from;

    /**
     * Количество услуг (по умолчанию 20, максимум 50)
     */
    private Integer size;

    /**
     * Фильтр услуг
     */
    private FilterInfo filter;

    /**
     * Язык названий услуг (EN, KY, RU)
     */
    private String locale;

    /**
     * Поиск по названию
     */
    private String query;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonPropertyOrder(alphabetic = true)
    public static class FilterInfo {
        /**
         * Массив статусов (ENABLED, DISABLED, INACTIVE)
         * По умолчанию: [ENABLED, DISABLED]
         */
        private List<String> status;

        /**
         * ID родительской категории
         * Если не указано - вернутся все услуги
         */
        private String parentId;
    }
}
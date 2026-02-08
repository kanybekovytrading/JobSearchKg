package job.search.kg.controller.admin;

import job.search.kg.dto.response.admin.PaymentAnalyticsResponse;
import job.search.kg.payment.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/admin/payments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin Payment Analytics", description = "Аналитика платежей (админ)")
public class AdminPaymentAnalyticsController {

    private final PaymentService paymentService;

    @GetMapping("/analytics")
    @Operation(summary = "Аналитика платежей с фильтром по датам")
    public ResponseEntity<PaymentAnalyticsResponse> getPaymentAnalytics(
            @Parameter(description = "Начальная дата (формат: yyyy-MM-dd'T'HH:mm:ss)")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime startDate,

            @Parameter(description = "Конечная дата (формат: yyyy-MM-dd'T'HH:mm:ss)")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime endDate
    ) {
        log.info("Get payment analytics: startDate={}, endDate={}", startDate, endDate);

        PaymentAnalyticsResponse analytics = paymentService.getPaymentAnalytics(startDate, endDate);
        return ResponseEntity.ok(analytics);
    }

    @GetMapping("/analytics/today")
    @Operation(summary = "Аналитика за сегодня")
    public ResponseEntity<PaymentAnalyticsResponse> getTodayAnalytics() {
        LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = LocalDateTime.now();

        log.info("Get today analytics");

        PaymentAnalyticsResponse analytics = paymentService.getPaymentAnalytics(startOfDay, endOfDay);
        return ResponseEntity.ok(analytics);
    }

    @GetMapping("/analytics/week")
    @Operation(summary = "Аналитика за неделю")
    public ResponseEntity<PaymentAnalyticsResponse> getWeekAnalytics() {
        LocalDateTime weekAgo = LocalDateTime.now().minusWeeks(1);
        LocalDateTime now = LocalDateTime.now();

        log.info("Get week analytics");

        PaymentAnalyticsResponse analytics = paymentService.getPaymentAnalytics(weekAgo, now);
        return ResponseEntity.ok(analytics);
    }

    @GetMapping("/analytics/month")
    @Operation(summary = "Аналитика за месяц")
    public ResponseEntity<PaymentAnalyticsResponse> getMonthAnalytics() {
        LocalDateTime monthAgo = LocalDateTime.now().minusMonths(1);
        LocalDateTime now = LocalDateTime.now();

        log.info("Get month analytics");

        PaymentAnalyticsResponse analytics = paymentService.getPaymentAnalytics(monthAgo, now);
        return ResponseEntity.ok(analytics);
    }

    @GetMapping("/analytics/all-time")
    @Operation(summary = "Аналитика за все время")
    public ResponseEntity<PaymentAnalyticsResponse> getAllTimeAnalytics() {
        log.info("Get all-time analytics");

        PaymentAnalyticsResponse analytics = paymentService.getPaymentAnalytics(null, null);
        return ResponseEntity.ok(analytics);
    }
}

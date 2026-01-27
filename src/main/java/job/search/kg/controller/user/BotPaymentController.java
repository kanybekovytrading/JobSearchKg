package job.search.kg.controller.user;

import job.search.kg.dto.request.user.CreatePaymentRequest;
import job.search.kg.service.user.BotPaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/bot/payments")
@RequiredArgsConstructor
public class BotPaymentController {

    private final BotPaymentService botPaymentService;
//
//    @PostMapping("/create")
//    public ResponseEntity<PaymentResponse> createPayment(
//            @RequestParam Long telegramId,
//            @RequestBody CreatePaymentRequest request) {
//        PaymentResponse payment = botPaymentService.createPayment(telegramId, request);
//        return ResponseEntity.status(HttpStatus.CREATED).body(payment);
//    }

    @PostMapping("/webhook")
    public ResponseEntity<Void> handleWebhook(@RequestBody Map<String, Object> webhookData) {
        botPaymentService.processWebhook(webhookData);
        return ResponseEntity.ok().build();
    }
}

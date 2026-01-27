package job.search.kg.service.user;

import job.search.kg.dto.response.user.ReferralInfoResponse;
import job.search.kg.entity.PointsTransaction;
import job.search.kg.entity.User;
import job.search.kg.telegram.TelegramService;
import job.search.kg.exceptions.ResourceNotFoundException;
import job.search.kg.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BotReferralService {

    private final UserRepository userRepository;
    private final BotPointsService pointsService;
    private final TelegramService telegramService;

    @Transactional
    public void processReferral(Long referrerId, Long newUserId) {
        User referrer = userRepository.findByTelegramId(referrerId)
                .orElseThrow(() -> new ResourceNotFoundException("Referrer not found"));

        User newUser = userRepository.findByTelegramId(newUserId)
                .orElseThrow(() -> new ResourceNotFoundException("New user not found"));

        // Начислить баллы реферу
        pointsService.addPoints(
                referrerId,
                50,
                PointsTransaction.TransactionType.REFERRAL,
                "Приглашение друга"
        );

        // Начислить бонус новому пользователю
        pointsService.addPoints(
                newUserId,
                10,
                PointsTransaction.TransactionType.REFERRAL,
                "Регистрация по реферальной ссылке"
        );

        // Уведомление реферу
        telegramService.sendMessage(
                referrerId,
                String.format(
                        "🎉 Ваш друг %s зарегистрировался!\n\n+50 баллов на ваш счёт!",
                        newUser.getFirstName()
                )
        );
    }

    @Transactional(readOnly = true)
    public ReferralInfoResponse getReferralInfo(Long telegramId) {
        User user = userRepository.findByTelegramId(telegramId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String referralLink = "https://t.me/work_kg_bot?start=" + user.getReferralCode();

//        // Подсчёт приглашённых
//        Long referralsCount = userRepository.count(
//                (root, query, cb) -> cb.equal(root.get("referrer"), user)
//        );

        ReferralInfoResponse response = new ReferralInfoResponse();
        response.setReferralCode(user.getReferralCode());
        response.setReferralLink(referralLink);
//        response.setReferralsCount(referralsCount.intValue());
        response.setRewardPerReferral(50);

        return response;
    }
}

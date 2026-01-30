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

        Long referralsCount = userRepository.countByReferrer(referrer);

        if(referralsCount >= 100){
            String message = getReferralLimitMessage(referrer.getLanguage());
            telegramService.sendMessage(referrerId, message);
            return;
        }
        // Начислить баллы реферу
        pointsService.addPoints(
                referrerId,
                100,
                PointsTransaction.TransactionType.REFERRAL,
                "Приглашение друга"
        );

        // Начислить бонус новому пользователю
        pointsService.addPoints(
                newUserId,
                50,
                PointsTransaction.TransactionType.REFERRAL,
                "Регистрация по реферальной ссылке"
        );

        String successMessage = getReferralSuccessMessage(
                referrer.getLanguage(),
                newUser.getFirstName()
        );
        telegramService.sendMessage(referrerId, successMessage);
    }

    @Transactional(readOnly = true)
    public ReferralInfoResponse getReferralInfo(Long telegramId) {
        User user = userRepository.findByTelegramId(telegramId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String referralLink = "https://t.me/bekacoder?start=" + user.getReferralCode();

        // Подсчёт приглашённых
        Long referralsCount = userRepository.countByReferrer(user);

        ReferralInfoResponse response = new ReferralInfoResponse();
        response.setReferralCode(user.getReferralCode());
        response.setReferralLink(referralLink);
       response.setReferralsCount(referralsCount.intValue());
        response.setRewardPerReferral(100);

        return response;
    }

    private String getReferralLimitMessage(User.Language language) {
        return switch (language) {
            case RU -> "🎉 Вы пригласили достаточно людей, лимит исчерпан!";
            case KY -> "🎉 Сиз жетиштүү адамдарды чакырдыңыз, лимит бүттү!";
            case EN -> "🎉 You have invited enough people, limit reached!";
        };
    }

    private String getReferralSuccessMessage(User.Language language, String friendName) {
        return switch (language) {
            case RU -> String.format(
                    "🎉 Ваш друг %s зарегистрировался!\n\n+100 баллов на ваш счёт!",
                    friendName
            );
            case KY -> String.format(
                    "🎉 Досуңуз %s катталды!\n\n+100 упай сиздин эсебиңизге!",
                    friendName
            );
            case EN -> String.format(
                    "🎉 Your friend %s has registered!\n\n+100 points added to your account!",
                    friendName
            );
        };
    }
}

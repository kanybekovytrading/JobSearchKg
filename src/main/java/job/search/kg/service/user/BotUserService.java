package job.search.kg.service.user;

import job.search.kg.dto.request.user.UserRegistrationRequest;
import job.search.kg.dto.request.user.UserUpdateRequest;
import job.search.kg.dto.response.user.UserProfileResponse;
import job.search.kg.entity.PointsTransaction;
import job.search.kg.entity.User;
import job.search.kg.exceptions.ResourceNotFoundException;
import job.search.kg.exceptions.UserAlreadyExistsException;
import job.search.kg.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BotUserService {
    private final UserRepository userRepository;
    private final BotReferralService botReferralService;
    private final BotPointsService pointsService;

    @Transactional
    public User registerUser(UserRegistrationRequest request) {
        if (userRepository.existsByTelegramId(request.getTelegramId())) {
            throw new UserAlreadyExistsException("Пользователь уже существует");
        }

        User user = new User();
        user.setTelegramId(request.getTelegramId());
        user.setUsername(request.getUsername());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhone(request.getPhone());
        user.setLanguage(request.getLanguage());
        user.setReferralCode("ref_" + UUID.randomUUID().toString().substring(0, 8));

        userRepository.save(user);

        pointsService.addPoints(user.getTelegramId(), 100, PointsTransaction.TransactionType.REGISTRATION, "Registration");
        if (request.getReferralCode() != null) {
            userRepository.findByReferralCode(request.getReferralCode())
                    .ifPresent(referrer -> {
                        user.setReferrer(referrer);
                        botReferralService.processReferral(referrer.getTelegramId(), user.getTelegramId());
                    });
        }

        return user;
    }

    @Transactional(readOnly = true)
    public User getUserByTelegramId(Long telegramId) {
        return userRepository.findByTelegramId(telegramId)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getUserProfile(Long telegramId) {
        User user = getUserByTelegramId(telegramId);

        UserProfileResponse response = new UserProfileResponse();
        response.setId(user.getId());
        response.setTelegramId(user.getTelegramId());
        response.setUsername(user.getUsername());
        response.setFirstName(user.getFirstName());
        response.setPhone(user.getPhone());
        response.setBalance(user.getBalance());
        response.setReferralCode(user.getReferralCode());

        return response;
    }

    @Transactional
    public void updateLanguage(Long telegramId, User.Language language) {
        User user = getUserByTelegramId(telegramId);
        user.setLanguage(language);
        userRepository.save(user);
    }

    @Transactional
    public void updateUser(Long telegramId, UserUpdateRequest request) {
        User user = getUserByTelegramId(telegramId);

        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }
        if (request.getUsername() != null) {
            user.setUsername(request.getUsername());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getLanguage() != null) {
            user.setLanguage(User.Language.valueOf(request.getLanguage().toUpperCase()));
        }

        userRepository.save(user);
    }
}

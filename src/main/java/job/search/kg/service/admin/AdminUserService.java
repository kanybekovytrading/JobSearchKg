package job.search.kg.service.admin;

import job.search.kg.entity.User;
import job.search.kg.exceptions.ResourceNotFoundException;
import job.search.kg.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Page<User> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = getUserById(id);
        userRepository.delete(user);
    }

    @Transactional
    public User banUser(Long id) {
        User user = getUserById(id);
        user.setIsBanned(true);
        return userRepository.save(user);
    }

    @Transactional
    public User unbanUser(Long id) {
        User user = getUserById(id);
        user.setIsBanned(false);
        return userRepository.save(user);
    }
}

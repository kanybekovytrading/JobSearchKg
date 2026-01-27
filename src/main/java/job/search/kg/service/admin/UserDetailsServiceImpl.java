package job.search.kg.service.admin;

import job.search.kg.entity.Admin;
import job.search.kg.repo.AdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Collections;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
    private final AdminRepository adminRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Admin admin = adminRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Admin not found with email: " + email
                ));

        return new AdminUserDetails(admin);
    }

    // Внутренний класс для UserDetails
    private static class AdminUserDetails implements UserDetails {

        private final Admin admin;

        public AdminUserDetails(Admin admin) {
            this.admin = admin;
        }

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            return Collections.singletonList(
                    new SimpleGrantedAuthority("ROLE_" + admin.getRole().name())
            );
        }

        @Override
        public String getPassword() {
            return admin.getPasswordHash();
        }

        @Override
        public String getUsername() {
            return admin.getEmail();
        }

        @Override
        public boolean isAccountNonExpired() {
            return true;
        }

        @Override
        public boolean isAccountNonLocked() {
            return true;
        }

        @Override
        public boolean isCredentialsNonExpired() {
            return true;
        }

        @Override
        public boolean isEnabled() {
            return admin.getIsActive();
        }

        public Admin getAdmin() {
            return admin;
        }
    }
}
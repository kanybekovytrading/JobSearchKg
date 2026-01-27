package job.search.kg.init;

import job.search.kg.entity.Admin;
import job.search.kg.entity.City;
import job.search.kg.repo.AdminRepository;
import job.search.kg.repo.CityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final CityRepository cityRepository;
    private final PasswordEncoder passwordEncoder;
    private final AdminRepository adminRepository;


    @Override
    public void run(String @NonNull ... args) {
        if (cityRepository.count() == 0) {
            List<City> cities = List.of(
                    City.builder().nameRu("Бишкек").nameEn("Bishkek").isActive(true).build(),
                    City.builder().nameRu("Ош").nameEn("Osh").isActive(true).build(),
                    City.builder().nameRu("Джалал-Абад").nameEn("Jalal-Abad").isActive(true).build(),
                    City.builder().nameRu("Каракол").nameEn("Karakol").isActive(true).build(),
                    City.builder().nameRu("Токмок").nameEn("Tokmok").isActive(true).build(),
                    City.builder().nameRu("Нарын").nameEn("Naryn").isActive(true).build(),
                    City.builder().nameRu("Талас").nameEn("Talas").isActive(true).build(),
                    City.builder().nameRu("Баткен").nameEn("Batken").isActive(true).build()
            );
            cityRepository.saveAll(cities);
            log.info("✅ Инициализировано {} городов", cities.size());
        }

        if (!adminRepository.existsByEmail("admin@gmail.com")){
            Admin admin = Admin.builder()
                    .name("Администратор")
                    .email("admin@gmail.com")
                    .role(Admin.AdminRole.ADMIN)
                    .passwordHash(passwordEncoder.encode("admin123")) // Реальный пароль хешируется
                    .isActive(true)
                    .createdAt(LocalDateTime.now())
                    .build();

            adminRepository.save(admin);
            log.info("Admin successfully added!");
        }
    }

}

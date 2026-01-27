package job.search.kg.service;

import job.search.kg.dto.response.CustomResponse;
import job.search.kg.entity.City;
import job.search.kg.entity.User;
import job.search.kg.exceptions.ResourceNotFoundException;
import job.search.kg.repo.CityRepository;
import job.search.kg.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CityService {

    private final CityRepository cityRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<CustomResponse> getAllActiveCities(Long telegramId) {
       User user =  userRepository.findByTelegramId(telegramId).orElseThrow(
                ()-> new ResourceNotFoundException("User not found")
        );
        List<City> cities =  cityRepository.findByIsActive(true);

    return  cities.stream().map(city -> {
            return CustomResponse.builder()
                    .id(city.getId())
                    .name(user.getLanguage().equals(User.Language.EN) ? city.getNameEn(): city.getNameRu())
                    .build();
        }).toList();
    }

    @Transactional(readOnly = true)
    public CustomResponse getCityById(Integer id, Long telegramId) {
        City city = cityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("City not found"));
        User user =  userRepository.findByTelegramId(telegramId).orElseThrow(
                ()-> new ResourceNotFoundException("User not found")
        );
        return CustomResponse.builder()
                .id(city.getId())
                .name(user.getLanguage().equals(User.Language.EN) ? city.getNameEn(): city.getNameRu())
                .build();
    }
}

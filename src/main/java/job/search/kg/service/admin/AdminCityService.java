package job.search.kg.service.admin;

import job.search.kg.dto.request.admin.CreateCityRequest;
import job.search.kg.entity.City;
import job.search.kg.exceptions.ResourceNotFoundException;
import job.search.kg.repo.CityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminCityService {

    private final CityRepository cityRepository;

    @Transactional(readOnly = true)
    public List<City> getAllCities() {
        return cityRepository.findAll();
    }

    @Transactional(readOnly = true)
    public City getCityById(Integer id) {
        return cityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("City not found"));
    }

    @Transactional
    public City createCity(CreateCityRequest request) {
        City city = new City();
        city.setNameRu(request.getNameRu());
        city.setNameEn(request.getNameEn());
        city.setIsActive(true);

        return cityRepository.save(city);
    }

    @Transactional
    public City updateCity(Integer id, CreateCityRequest request) {
        City city = getCityById(id);

        if (request.getNameRu() != null) {
            city.setNameRu(request.getNameRu());
        }
        if (request.getNameEn() != null) {
            city.setNameEn(request.getNameEn());
        }
        if (request.getIsActive() != null) {
            city.setIsActive(request.getIsActive());
        }

        return cityRepository.save(city);
    }

    @Transactional
    public void deleteCity(Integer id) {
        City city = getCityById(id);
        cityRepository.delete(city);
    }
}

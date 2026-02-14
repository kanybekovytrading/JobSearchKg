package job.search.kg.service;

import org.springframework.stereotype.Service;

@Service
public class LocationService {

    /**
     * Вычисляет расстояние между двумя точками по формуле Haversine
     * @param lat1 Широта первой точки
     * @param lon1 Долгота первой точки
     * @param lat2 Широта второй точки
     * @param lon2 Долгота второй точки
     * @return Расстояние в километрах
     */
    public double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int EARTH_RADIUS_KM = 6371; // Радиус Земли в километрах

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_KM * c;
    }
    /**
     * Проверяет, находится ли точка в пределах указанного радиуса
     * @param lat1 Широта первой точки
     * @param lon1 Долгота первой точки
     * @param lat2 Широта второй точки
     * @param lon2 Долгота второй точки
     * @param radiusKm Радиус в километрах
     * @return true если в пределах радиуса
     */
    public boolean isWithinRadius(double lat1, double lon1, double lat2, double lon2, double radiusKm) {
        double distance = calculateDistance(lat1, lon1, lat2, lon2);
        return distance <= radiusKm;
    }
}

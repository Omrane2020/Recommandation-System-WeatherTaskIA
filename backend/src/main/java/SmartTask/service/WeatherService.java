package SmartTask.service;

import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class WeatherService {

    private final String API_KEY = "d890ac1bf939d72894a57d2057a4ba14";

    private final String WEATHER_URL = "https://api.openweathermap.org/data/2.5/weather?q=%s&units=metric&lang=fr&appid=%s";
    private final String FORECAST_URL = "https://api.openweathermap.org/data/2.5/forecast?q=%s&units=metric&lang=fr&appid=%s";

    private final RestTemplate restTemplate = new RestTemplate();

    public JSONObject getWeather(String city) {
        String url = String.format(WEATHER_URL, city, API_KEY);
        String response = restTemplate.getForObject(url, String.class);
        return new JSONObject(response);
    }

    public JSONObject getForecast(String city) {
        String url = String.format(FORECAST_URL, city, API_KEY);
        String response = restTemplate.getForObject(url, String.class);
        return new JSONObject(response); 
    }
}

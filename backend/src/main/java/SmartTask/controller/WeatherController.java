package SmartTask.controller;

import SmartTask.service.WeatherService;
import org.json.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/weather")
@CrossOrigin(origins = "http://localhost:5173/")
public class WeatherController {

    private final WeatherService weatherService;

    public WeatherController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    // Endpoint pour la météo actuelle
    @GetMapping("/{city}")
    public String getWeather(@PathVariable String city) {
        JSONObject json = weatherService.getWeather(city);
        return json.toString();
    }

    // Endpoint pour la prévision 5 jours
    @GetMapping("/forecast/{city}")
    public ResponseEntity<?> getForecast(@PathVariable String city) {
        JSONObject forecast = weatherService.getForecast(city);

        // Debug : afficher la réponse brute dans la console
        System.out.println("Forecast raw response: " + forecast.toString());

        // S'assurer que "list" existe
        if (!forecast.has("list")) {
            forecast.put("list", new org.json.JSONArray());
        }


        return ResponseEntity.ok(forecast.toMap());
    }
}

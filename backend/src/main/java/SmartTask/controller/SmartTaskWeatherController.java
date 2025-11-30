package SmartTask.controller;

import org.json.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import SmartTask.dto.CheckWeatherRequest;
import SmartTask.dto.WeatherRecommendationResponse;
import SmartTask.model.Activity;
import SmartTask.model.Recommendation;
import SmartTask.service.SmartWeatherService;
import SmartTask.service.WeatherActivityApiClient;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:5173")
public class SmartTaskWeatherController {

    private final SmartWeatherService weatherService;
    private final WeatherActivityApiClient activityApiClient;

    public SmartTaskWeatherController(SmartWeatherService weatherService,
            WeatherActivityApiClient activityApiClient) {
        this.weatherService = weatherService;
        this.activityApiClient = activityApiClient;
    }

    @GetMapping("/health")
    public String healthCheck() {
        return "API SmartTask is running on port 5174!";
    }

    @GetMapping("/activities")
    public ResponseEntity<List<Activity>> getActivities() {
        try {
            List<Activity> activities = activityApiClient.getAllActivities();
            return ResponseEntity.ok(activities);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/activities/by-tags")
    public ResponseEntity<List<Activity>> getActivitiesByTags(@RequestParam List<String> tags) {
        try {
            List<Activity> activities = activityApiClient.getActivitiesByTags(tags);
            return ResponseEntity.ok(activities);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/activities/search")
    public ResponseEntity<List<Activity>> searchActivities(@RequestParam String keyword) {
        try {
            List<Activity> activities = activityApiClient.searchActivities(keyword);
            return ResponseEntity.ok(activities);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/tasks/check-weather")
    public ResponseEntity<?> checkWeather(@RequestBody CheckWeatherRequest request) {
        try {
            
            Activity activity = activityApiClient.getActivityById(request.getActivityId());

            if (activity == null) {
                return ResponseEntity.badRequest().body("Activité non trouvée avec l'ID: " + request.getActivityId());
            }

            JSONObject meteo = weatherService.getWeather(request.getCity());

            double temp = meteo.getJSONObject("main").getDouble("temp");
            double humidity = meteo.getJSONObject("main").getDouble("humidity");
            double wind = meteo.getJSONObject("wind").getDouble("speed");
            String conditions = meteo.getJSONArray("weather").getJSONObject(0).getString("description");

            double lat = meteo.getJSONObject("coord").getDouble("lat");
            double lon = meteo.getJSONObject("coord").getDouble("lon");
            double uvIndex = weatherService.getUVIndex(lat, lon);

            int score = weatherService.evaluateActivity(meteo, activity.getTags());
            String message = weatherService.recommendationMessage(score);

            Recommendation recommendation = new Recommendation(score, message);
            recommendation.setDetails(temp, conditions, humidity, wind, uvIndex);

            // Utiliser WeatherRecommendationResponse au lieu de setActivity
            WeatherRecommendationResponse response = new WeatherRecommendationResponse(activity, recommendation);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new JSONObject()
                            .put("error", true)
                            .put("message", e.getMessage())
                            .toMap());
        }
    }

    @GetMapping("/recommendations")
    public ResponseEntity<?> getWeatherBasedRecommendations(@RequestParam String city) {
        try {
            JSONObject meteo = weatherService.getWeather(city);
            double temp = meteo.getJSONObject("main").getDouble("temp");
            double humidity = meteo.getJSONObject("main").getDouble("humidity");
            double wind = meteo.getJSONObject("wind").getDouble("speed");
            String conditions = meteo.getJSONArray("weather").getJSONObject(0).getString("description");

            List<Activity> allActivities = activityApiClient.getAllActivities();

            List<WeatherRecommendationResponse> recommendations = allActivities.stream()
                    .map(activity -> {
                        int score = weatherService.evaluateActivity(meteo, activity.getTags());
                        String message = weatherService.recommendationMessage(score);

                        Recommendation rec = new Recommendation(score, message);
                        rec.setDetails(temp, conditions, humidity, wind, 0);

                        return new WeatherRecommendationResponse(activity, rec);
                    })
                    .filter(response -> response.getRecommendation().getScore() >= 5)
                    .sorted((r1, r2) -> Integer.compare(
                            r2.getRecommendation().getScore(),
                            r1.getRecommendation().getScore()))
                    .limit(10)
                    .toList();

            return ResponseEntity.ok(recommendations);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur: " + e.getMessage());
        }
    }

    @PostMapping("/activities")
    public ResponseEntity<Activity> createActivity(@RequestBody Activity activity) {
        try {
            Activity createdActivity = activityApiClient.createActivity(activity);
            return ResponseEntity.ok(createdActivity);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
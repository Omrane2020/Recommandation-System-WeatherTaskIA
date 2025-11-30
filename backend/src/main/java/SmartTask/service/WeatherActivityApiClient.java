package SmartTask.service;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import SmartTask.model.Activity;

import java.util.Arrays;
import java.util.List;

@Service
public class WeatherActivityApiClient {

    private final String BASE_URL = "http://localhost:8080/api/activities";
    private final RestTemplate restTemplate;

    public WeatherActivityApiClient() {
        this.restTemplate = new RestTemplate();
    }

    public List<Activity> getAllActivities() {
        try {
            ResponseEntity<Activity[]> response = restTemplate.getForEntity(BASE_URL, Activity[].class);
            return Arrays.asList(response.getBody());
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la récupération des activités: " + e.getMessage());
        }
    }

    public Activity getActivityById(Long id) {
        try {
            String url = BASE_URL + "/" + id;
            ResponseEntity<Activity> response = restTemplate.getForEntity(url, Activity.class);
            return response.getBody();
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la récupération de l'activité: " + e.getMessage());
        }
    }

    public List<Activity> getActivitiesByTags(List<String> tags) {
        try {
            String url = UriComponentsBuilder.fromHttpUrl(BASE_URL + "/by-tags")
                    .queryParam("tags", tags.toArray())
                    .toUriString();
            
            ResponseEntity<Activity[]> response = restTemplate.getForEntity(url, Activity[].class);
            return Arrays.asList(response.getBody());
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la recherche par tags: " + e.getMessage());
        }
    }

    public List<Activity> searchActivities(String keyword) {
        try {
            String url = BASE_URL + "/search?param=" + keyword;
            ResponseEntity<Activity[]> response = restTemplate.getForEntity(url, Activity[].class);
            return Arrays.asList(response.getBody());
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la recherche: " + e.getMessage());
        }
    }

    public Activity createActivity(Activity activity) {
        try {
            ResponseEntity<Activity> response = restTemplate.postForEntity(BASE_URL, activity, Activity.class);
            return response.getBody();
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la création: " + e.getMessage());
        }
    }
}
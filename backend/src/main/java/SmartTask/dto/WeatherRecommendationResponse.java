package SmartTask.dto;

import SmartTask.model.Activity;
import SmartTask.model.Recommendation;

public class WeatherRecommendationResponse {
    private Activity activity;
    private Recommendation recommendation;

    public WeatherRecommendationResponse() {
    }

    public WeatherRecommendationResponse(Activity activity, Recommendation recommendation) {
        this.activity = activity;
        this.recommendation = recommendation;
    }

    // Getters et Setters
    public Activity getActivity() {
        return activity;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public Recommendation getRecommendation() {
        return recommendation;
    }

    public void setRecommendation(Recommendation recommendation) {
        this.recommendation = recommendation;
    }
}
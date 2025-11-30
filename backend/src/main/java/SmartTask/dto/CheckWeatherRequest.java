package SmartTask.dto;

public class CheckWeatherRequest {
    private Long activityId;
    private String city;

    // Constructeurs
    public CheckWeatherRequest() {
    }

    public CheckWeatherRequest(Long activityId, String city) {
        this.activityId = activityId;
        this.city = city;
    }

    // Getters et Setters
    public Long getActivityId() {
        return activityId;
    }

    public void setActivityId(Long activityId) {
        this.activityId = activityId;

    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }
}

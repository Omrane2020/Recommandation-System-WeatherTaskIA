package SmartTask.model;

import java.util.List;

public class Activity {
    private Long id;
    private String icon;
    private String title;
    private List<String> tags;
    private List<String> weatherTags;

    // Constructeurs
    public Activity() {
    }

    public Activity(Long id, String title, List<String> weatherTags) {
        this.id = id;
        this.title = title;
        this.weatherTags = weatherTags;
    }

    public Activity(Long id, String icon, String title, List<String> tags) {
        this.id = id;
        this.icon = icon;
        this.title = title;
        this.tags = tags;
    }

    // Getters et Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public List<String> getWeatherTags() {
        return weatherTags;
    }

    public void setWeatherTags(List<String> weatherTags) {
        this.weatherTags = weatherTags;
    }
}
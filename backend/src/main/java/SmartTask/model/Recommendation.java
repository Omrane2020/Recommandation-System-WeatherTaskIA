package SmartTask.model;

public class Recommendation {
    private int score;
    private String recommendation;
    private WeatherDetails details;
    private Activity activity; // ⚡ Ajout du champ Activity

    // Constructeurs
    public Recommendation() {
    }

    public Recommendation(int score, String recommendation) {
        this.score = score;
        this.recommendation = recommendation;
    }

    // Getters et Setters
    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public String getRecommendation() {
        return recommendation;
    }

    public void setRecommendation(String recommendation) {
        this.recommendation = recommendation;
    }

    public WeatherDetails getDetails() {
        return details;
    }

    public void setDetails(WeatherDetails details) {
        this.details = details;
    }

    public Activity getActivity() {
        return activity;
    } // getter

    public void setActivity(Activity activity) {
        this.activity = activity;
    } // setter

    // Méthode utilitaire pour définir les détails météo
    public void setDetails(double temperature, String conditions, double humidity, double windSpeed, double uvIndex) {
        this.details = new WeatherDetails(temperature, conditions, humidity, windSpeed, uvIndex);
    }

    // Classe interne pour les détails météo
    public static class WeatherDetails {
        private double temperature;
        private String conditions;
        private double humidity;
        private double windSpeed;
        private double uvIndex;

        public WeatherDetails(double temperature, String conditions, double humidity, double windSpeed,
                double uvIndex) {
            this.temperature = temperature;
            this.conditions = conditions;
            this.humidity = humidity;
            this.windSpeed = windSpeed;
            this.uvIndex = uvIndex;
        }

        // Getters
        public double getTemperature() {
            return temperature;
        }

        public String getConditions() {
            return conditions;
        }

        public double getHumidity() {
            return humidity;
        }

        public double getWindSpeed() {
            return windSpeed;
        }

        public double getUvIndex() {
            return uvIndex;
        }
    }
}
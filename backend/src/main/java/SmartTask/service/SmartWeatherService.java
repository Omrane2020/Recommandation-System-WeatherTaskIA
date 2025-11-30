package SmartTask.service;

import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class SmartWeatherService {

    private static final String API_KEY = "d890ac1bf939d72894a57d2057a4ba14";
    private static final String WEATHER_URL = "https://api.openweathermap.org/data/2.5/weather?q=%s&units=metric&appid=%s";
    private static final String UV_URL = "https://api.openweathermap.org/data/2.5/uvi?lat=%s&lon=%s&appid=%s";

    public JSONObject getWeather(String city) {
        String url = String.format(WEATHER_URL, city, API_KEY);
        RestTemplate client = new RestTemplate();
        String response = client.getForObject(url, String.class);
        return new JSONObject(response);
    }

    public double getUVIndex(double lat, double lon) {
        try {
            String url = String.format(UV_URL, lat, lon, API_KEY);
            RestTemplate client = new RestTemplate();
            String response = client.getForObject(url, String.class);
            JSONObject uvData = new JSONObject(response);
            return uvData.getDouble("value");
        } catch (Exception e) {
            System.out.println("Erreur récupération UV: " + e.getMessage());
            return 3.0; // Valeur par défaut modérée
        }
    }

    public int evaluateActivity(JSONObject forecast, List<String> tags) {
        int score = 100;

        // Extraction des données météo complètes
        double temp = forecast.getJSONObject("main").getDouble("temp");
        double feelsLike = forecast.getJSONObject("main").getDouble("feels_like");
        double humidity = forecast.getJSONObject("main").getDouble("humidity");
        double wind = forecast.getJSONObject("wind").getDouble("speed");
        double rain = forecast.has("rain") ? forecast.getJSONObject("rain").optDouble("1h", 0) : 0;
        double snow = forecast.has("snow") ? forecast.getJSONObject("snow").optDouble("1h", 0) : 0;

        // Coordonnées pour l'index UV
        double lat = forecast.getJSONObject("coord").getDouble("lat");
        double lon = forecast.getJSONObject("coord").getDouble("lon");
        double uvIndex = getUVIndex(lat, lon);

        // Conditions générales
        String weatherMain = forecast.getJSONArray("weather").getJSONObject(0).getString("main");
        String weatherDescription = forecast.getJSONArray("weather").getJSONObject(0).getString("description");

        System.out.println("Météo détaillée - Temp: " + temp + "°C (Ressentie: " + feelsLike + "°C), " +
                "Humidité: " + humidity + "%, Vent: " + wind + "m/s, " +
                "Pluie: " + rain + "mm, Neige: " + snow + "mm, UV: " + uvIndex);
        System.out.println("Conditions: " + weatherMain + " - " + weatherDescription);
        System.out.println("Tags activité: " + tags);

        // Si activité indoor, météo n'a pas d'importance
        if (tags.contains("indoor")) {
            return 100;
        }

        // === ÉVALUATION DE LA TEMPÉRATURE (30% du poids) ===
        double tempScore = calculateTemperatureScore(temp, feelsLike, humidity, tags);

        // === ÉVALUATION DES PRÉCIPITATIONS (25% du poids) ===
        double precipitationScore = calculatePrecipitationScore(rain, snow, weatherMain, tags);

        // === ÉVALUATION DU VENT (15% du poids) ===
        double windScore = calculateWindScore(wind, tags);

        // === ÉVALUATION DE LA VISIBILITÉ/HUMIDITÉ (10% du poids) ===
        double visibilityScore = calculateVisibilityScore(humidity, weatherMain, tags);

        // === NOUVEAU: ÉVALUATION DE L'INDEX UV (20% du poids) ===
        double uvScore = calculateUVScore(uvIndex, tags);

        // Calcul du score pondéré
        score = (int) Math.round(
                tempScore * 0.3 +
                        precipitationScore * 0.25 +
                        windScore * 0.15 +
                        visibilityScore * 0.1 +
                        uvScore * 0.2);

        // Ajustements basés sur la combinaison de conditions
        score = applyCombinationAdjustments(score, temp, rain, wind, humidity, uvIndex, tags);

        System.out.println("Scores détaillés - Temp: " + tempScore + ", Précip: " + precipitationScore +
                ", Vent: " + windScore + ", Visibilité: " + visibilityScore + ", UV: " + uvScore);
        System.out.println("Score final: " + score);

        return Math.max(0, Math.min(100, score));
    }

    private double calculateUVScore(double uvIndex, List<String> tags) {
        double score = 100;

        if (tags.contains("sun-critical")) {
            // Activités sensibles au soleil (randonnée, photographie)
            if (uvIndex >= 11)
                score -= 80; // UV extrême - danger
            else if (uvIndex >= 8)
                score -= 60; // UV très fort
            else if (uvIndex >= 6)
                score -= 40; // UV fort
            else if (uvIndex >= 3)
                score -= 10; // UV modéré
            // Bonus pour UV faible (conditions idéales pour les activités solaires)
            else if (uvIndex < 3)
                score += 15;
        } else if (tags.contains("sun-sensitive")) {
            // Activités modérément sensibles au soleil
            if (uvIndex >= 11)
                score -= 60;
            else if (uvIndex >= 8)
                score -= 40;
            else if (uvIndex >= 6)
                score -= 20;
            else if (uvIndex >= 3)
                score -= 5;
        } else {
            // Activités peu sensibles au soleil
            if (uvIndex >= 11)
                score -= 30;
            else if (uvIndex >= 8)
                score -= 15;
            else if (uvIndex >= 6)
                score -= 5;
        }

        return Math.max(0, Math.min(100, score));
    }

    private double calculateTemperatureScore(double temp, double feelsLike, double humidity, List<String> tags) {
        double baseScore = 100;
        double effectiveTemp = feelsLike; // Utilise la température ressentie

        // Indice de chaleur pour les températures élevées
        if (effectiveTemp > 27 && humidity > 70) {
            effectiveTemp += (humidity - 70) * 0.1;
        }

        // Indice de refroidissement éolien pour les basses températures
        if (effectiveTemp < 10 && humidity > 80) {
            effectiveTemp -= (humidity - 80) * 0.05;
        }

        if (tags.contains("temp-sensitive")) {
            // Pour activités très sensibles à la température
            if (effectiveTemp < 8 || effectiveTemp > 35)
                return 20;
            if (effectiveTemp < 12 || effectiveTemp > 30)
                return 40;
            if (effectiveTemp < 16 || effectiveTemp > 27)
                return 70;
            if (effectiveTemp >= 20 && effectiveTemp <= 24)
                return 95;
            return 80;
        } else if (tags.contains("temp-min")) {
            // Activités sensibles au froid (piscine, baignade)
            if (effectiveTemp < 18)
                baseScore -= 70;
            else if (effectiveTemp < 22)
                baseScore -= 40;
            else if (effectiveTemp < 25)
                baseScore -= 20;

            // Bonus pour températures chaudes idéales
            if (effectiveTemp >= 25 && effectiveTemp <= 30)
                baseScore += 20;
            else if (effectiveTemp > 30)
                baseScore -= 10; // Trop chaud
        } else if (tags.contains("temp-max")) {
            // Activités sensibles à la chaleur (sport intense)
            if (effectiveTemp > 32)
                baseScore -= 70;
            else if (effectiveTemp > 28)
                baseScore -= 50;
            else if (effectiveTemp > 25)
                baseScore -= 30;

            // Bonus pour températures fraîches idéales
            if (effectiveTemp >= 15 && effectiveTemp <= 22)
                baseScore += 20;
            else if (effectiveTemp < 10)
                baseScore -= 20; // Trop froid
        } else {
            // Conditions idéales générales
            if (effectiveTemp >= 18 && effectiveTemp <= 25)
                baseScore += 15;
            else if (effectiveTemp < 5 || effectiveTemp > 35)
                baseScore -= 50;
            else if (effectiveTemp < 10 || effectiveTemp > 30)
                baseScore -= 30;
            else if (effectiveTemp < 15 || effectiveTemp > 27)
                baseScore -= 15;
        }

        return Math.max(0, Math.min(100, baseScore));
    }

    private double calculatePrecipitationScore(double rain, double snow, String weatherMain, List<String> tags) {
        double score = 100;

        if (tags.contains("rain-prohibited")) {
            // Pénalités sévères pour la pluie
            if (rain > 5 || weatherMain.equals("Rain"))
                score -= 90;
            else if (rain > 2)
                score -= 70;
            else if (rain > 0.5)
                score -= 50;
            else if (rain > 0.1)
                score -= 30;

            // Neige = conditions impossibles
            if (snow > 0 || weatherMain.equals("Snow"))
                score -= 95;
        } else if (tags.contains("rain-sensitive")) {
            // Pénalités modérées
            if (rain > 2 || weatherMain.equals("Rain"))
                score -= 60;
            else if (rain > 0.5)
                score -= 40;
            else if (rain > 0.1)
                score -= 20;

            if (snow > 0)
                score -= 50;
        } else {
            // Légères pénalités pour précipitations importantes
            if (rain > 5)
                score -= 40;
            else if (rain > 2)
                score -= 20;
            else if (rain > 0.5)
                score -= 10;

            if (snow > 0)
                score -= 30;
        }

        // Conditions météo sévères
        if (weatherMain.equals("Thunderstorm"))
            score -= 60;
        else if (weatherMain.equals("Drizzle"))
            score -= 15;

        return Math.max(0, Math.min(100, score));
    }

    private double calculateWindScore(double wind, List<String> tags) {
        double score = 100;

        if (tags.contains("wind-sensitive")) {
            // Activités très sensibles au vent (yoga, badminton, parapente)
            if (wind > 15)
                score -= 85; // Vent fort
            else if (wind > 10)
                score -= 65; // Vent modéré-fort
            else if (wind > 6)
                score -= 45; // Vent modéré
            else if (wind > 3)
                score -= 25; // Léger vent
            // Bonus pour vent très faible
            else if (wind < 1)
                score += 10;
        } else if (tags.contains("wind-moderate")) {
            // Activités modérément sensibles (vélo, randonnée)
            if (wind > 20)
                score -= 70;
            else if (wind > 15)
                score -= 50;
            else if (wind > 10)
                score -= 30;
            else if (wind > 5)
                score -= 15;
        } else {
            // Activités peu sensibles
            if (wind > 25)
                score -= 40;
            else if (wind > 15)
                score -= 20;
            else if (wind > 8)
                score -= 10;
        }

        // Bonus pour vent léger par temps chaud
        if (wind > 1 && wind < 4)
            score += 5;

        return Math.max(0, Math.min(100, score));
    }

    private double calculateVisibilityScore(double humidity, String weatherMain, List<String> tags) {
        double score = 100;

        // Pénalités pour humidité élevée
        if (humidity > 90)
            score -= 40;
        else if (humidity > 80)
            score -= 25;
        else if (humidity > 70)
            score -= 10;

        // Conditions de visibilité réduite
        if (weatherMain.equals("Fog") || weatherMain.equals("Mist")) {
            score -= 50;
        } else if (weatherMain.equals("Haze") || weatherMain.equals("Smoke")) {
            score -= 30;
        }

        // Bonus pour conditions claires et air sec
        if ((weatherMain.equals("Clear") || weatherMain.equals("Few clouds")) && humidity < 60) {
            score += 15;
        }

        return Math.max(0, Math.min(100, score));
    }

    private int applyCombinationAdjustments(int score, double temp, double rain, double wind,
            double humidity, double uvIndex, List<String> tags) {
        int adjustedScore = score;

        // Combinaisons défavorables
        if (rain > 0.1 && wind > 8) {
            adjustedScore -= 15; // Pluie + vent = conditions très désagréables
        }

        if (temp > 30 && humidity > 80) {
            adjustedScore -= 25; // Chaleur humide extrêmement inconfortable
        }

        if (temp < 5 && wind > 10) {
            adjustedScore -= 30; // Refroidissement éolien sévère
        }

        if (uvIndex > 8 && tags.contains("sun-critical")) {
            adjustedScore -= 20; // Fort UV + activité sensible = danger
        }

        // Combinaisons favorables
        if (temp >= 18 && temp <= 25 && rain == 0 && wind < 5 && uvIndex >= 3 && uvIndex <= 6) {
            adjustedScore += 15; // Conditions parfaites pour activités extérieures
        }

        if (tags.contains("indoor") && (rain > 0 || temp < 5 || temp > 30)) {
            adjustedScore += 10; // Mauvais temps rend les activités indoor plus attractives
        }

        return Math.max(0, Math.min(100, adjustedScore));
    }

    public String recommendationMessage(int score) {
        if (score >= 90)
            return "🎯 EXCELLENT : Conditions parfaites pour votre activité !";
        if (score >= 75)
            return "🟢 TRÈS BON : Conditions très favorables, idéal pour sortir.";
        if (score >= 60)
            return "🟢 BON : Bonnes conditions, activité recommandée.";
        if (score >= 50)
            return "🟡 ACCEPTABLE : Conditions acceptables avec quelques réserves.";
        if (score >= 35)
            return "🟠 MOYEN : Conditions mitigées, prévoir des alternatives.";
        if (score >= 20)
            return "🟠 DIFFICILE : Conditions défavorables, déconseillé sauf nécessité.";
        if (score >= 10)
            return "🔴 MAUVAIS : Conditions très défavorables, fortement déconseillé.";
        return "🔴 CRITIQUE : Conditions extrêmes, activité impossible !";
    }
}
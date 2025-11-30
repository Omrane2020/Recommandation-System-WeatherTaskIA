import { useState, useEffect } from "react";
import './Weather.css';
import { useNavigate } from "react-router-dom";

interface WeatherData {
  temp: number;
  feels_like: number;
  description: string;
  icon: string;
  humidity: number;
  wind_speed: number;
  pressure: number;
  visibility: number;
  sunrise: number;
  sunset: number;
  city: string;
  country: string;
}

interface ForecastData {
  date: number;
  temp_min: number;
  temp_max: number;
  icon: string;
  description: string;
}

const Weather = () => {
  const [weather, setWeather] = useState<WeatherData | null>(null);
  const [forecast, setForecast] = useState<ForecastData[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);
  const [city, setCity] = useState("Monastir");
  const [searchInput, setSearchInput] = useState("");
  const [isCelsius, setIsCelsius] = useState(true);

  // Liste des pays à filtrer
  const blockedCountries = ['IL']; // Codes ISO des pays à bloquer

  const fetchWeatherData = async (cityName: string) => {
    try {
      setLoading(true);
      setError(null);


      const weatherRes = await fetch(`http://localhost:8081/api/weather/${cityName}`);


      if (!weatherRes.ok) throw new Error("Ville introuvable");

      const weatherData = await weatherRes.json();

      // Vérifier si le pays est bloqué
      const countryCode = weatherData.sys.country;
      if (blockedCountries.includes(countryCode)) {
        throw new Error("GPS en panne : impossible de localiser ce pays fictif. Palestine localisée avec succès! 🎯");
      }

      const forecastRes = await fetch(`http://localhost:8081/api/weather/forecast/${cityName}`);


      const forecastData = await forecastRes.json();

      const currentWeather: WeatherData = {
        temp: Math.round(weatherData.main.temp),
        feels_like: Math.round(weatherData.main.feels_like),
        description: weatherData.weather[0].description,
        icon: weatherData.weather[0].icon,
        humidity: weatherData.main.humidity,
        wind_speed: Math.round(weatherData.wind.speed * 3.6),
        pressure: weatherData.main.pressure,
        visibility: weatherData.visibility / 1000,
        sunrise: weatherData.sys.sunrise,
        sunset: weatherData.sys.sunset,
        city: weatherData.name,
        country: weatherData.sys.country,
      };

      const dailyForecasts: ForecastData[] = [];
      const processedDays = new Set();
      console.log(forecastData);
      const forecastList = forecastData.list || [];
      forecastList.forEach((item: any) => {
        const date = new Date(item.dt * 1000).toLocaleDateString('fr-FR');
        if (!processedDays.has(date) && dailyForecasts.length < 5) {
          processedDays.add(date);
          dailyForecasts.push({
            date: item.dt,
            temp_min: Math.round(item.main.temp_min),
            temp_max: Math.round(item.main.temp_max),
            icon: item.weather[0].icon,
            description: item.weather[0].description,
          });
        }
      });
      console.log('Forecast list:', forecastList);


      setWeather(currentWeather);
      setForecast(dailyForecasts);
    } catch (err: any) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchWeatherData(city);
  }, [city]);

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    if (searchInput.trim()) {
      setCity(searchInput.trim());
    }
  };

  const formatTime = (timestamp: number) => {
    return new Date(timestamp * 1000).toLocaleTimeString('fr-FR', {
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  const getWeatherClass = () => {
    if (!weather) return "weather-app weather-cold";

    const temp = weather.temp;
    if (temp < 0) return "weather-app weather-freezing";
    if (temp < 10) return "weather-app weather-cold";
    if (temp < 20) return "weather-app weather-cool";
    if (temp < 30) return "weather-app weather-warm";
    return "weather-app weather-hot";
  };

  const toggleTemperatureUnit = () => {
    setIsCelsius(!isCelsius);
  };


// Dans le composant
const navigate = useNavigate();

const handleGoToTasks = () => {
  navigate(`/task`);
};

  const convertTemp = (temp: number) => {
    if (isCelsius) return temp;
    return Math.round((temp * 9 / 5) + 32);
  };

  if (loading && !weather) {
    return (
      <div className={`${getWeatherClass()} loading`}>
        <div className="weather-container">
          <div className="loading-card">
            <div className="loading-spinner"></div>
            <p className="loading-text">Chargement de la météo...</p>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className={getWeatherClass()}>
      <div className="weather-container">

        {/* Header avec recherche */}
        <div className="weather-header">
          <form onSubmit={handleSearch} className="search-form">
            <div className="search-container">
              <input
                type="text"
                value={searchInput}
                onChange={(e) => setSearchInput(e.target.value)}
                placeholder="Rechercher une ville..."
                className="search-input"
              />
              <div className="search-icon">
                <svg className="search-svg" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
                </svg>
              </div>
              <button type="submit" className="search-button">
                Rechercher
              </button>
              {weather && (
                <div className="weather-actions">
                  <button
                    className="task-button"
                    onClick={() => handleGoToTasks()}
                  >
                    Task Insight
                  </button>
                </div>
              )}
            </div>
          </form>

          {/* Switch température */}
          <div className="temp-toggle">
            <div className="temp-switch">
              <button
                onClick={() => setIsCelsius(true)}
                className={`temp-option ${isCelsius ? 'active' : ''}`}
              >
                °C
              </button>
              <button
                onClick={() => setIsCelsius(false)}
                className={`temp-option ${!isCelsius ? 'active' : ''}`}
              >
                °F
              </button>
            </div>
          </div>
        </div>

        {error && (
          <div className="error-container">
            <p className="error-message">{error}</p>
            {error === "Cette localisation n'est pas disponible" && (
              <p className="error-suggestion">
                Veuillez rechercher une autre ville.
              </p>
            )}
          </div>
        )}

        {weather && (
          <div className="weather-content">

            {/* Carte principale */}
            <div className="weather-card main-card">
              <div className="weather-main-info">

                {/* Info principale */}
                <div className="location-info">
                  <h1 className="city-name">
                    {weather.city}, {weather.country}
                  </h1>
                  <p className="current-date">
                    {new Date().toLocaleDateString('fr-FR', {
                      weekday: 'long',
                      year: 'numeric',
                      month: 'long',
                      day: 'numeric'
                    })}
                  </p>

                  <div className="temperature-section">
                    <div>
                      <h2 className="current-temp">
                        {convertTemp(weather.temp)}°{isCelsius ? 'C' : 'F'}
                      </h2>
                      <p className="feels-like">
                        Ressenti {convertTemp(weather.feels_like)}°
                      </p>
                    </div>
                  </div>

                  <p className="weather-description">
                    {weather.description}
                  </p>
                </div>

                {/* Icône météo */}
                <div className="weather-icon-container">
                  <img
                    src={`http://openweathermap.org/img/wn/${weather.icon}@4x.png`}
                    alt={weather.description}
                    className="weather-icon-img"
                  />
                </div>
              </div>
            </div>

            {/* Statistiques météo */}
            <div className="stats-grid">
              {[
                { icon: "💧", label: "Humidité", value: `${weather.humidity}%` },
                { icon: "💨", label: "Vent", value: `${weather.wind_speed} km/h` },
                { icon: "🌅", label: "Lever", value: formatTime(weather.sunrise) },
                { icon: "🌇", label: "Coucher", value: formatTime(weather.sunset) },
              ].map((stat, index) => (
                <div key={index} className="stat-card">
                  <div className="stat-icon">{stat.icon}</div>
                  <p className="stat-label">{stat.label}</p>
                  <p className="stat-value">{stat.value}</p>
                </div>
              ))}
            </div>

            {/* Prévisions */}
            <div className="weather-card forecast-section">
              <h3 className="section-title">Prévisions sur 5 jours</h3>
              <div className="forecast-grid">
                {forecast.map((day, index) => (
                  <div key={index} className="forecast-card">
                    <p className="forecast-day">
                      {index === 0
                        ? "Aujourd'hui"
                        : new Date(day.date * 1000).toLocaleDateString('fr-FR', { weekday: 'short' })}
                    </p>

                    <img
                      src={`http://openweathermap.org/img/wn/${day.icon}@2x.png`}
                      alt={day.description}
                      className="forecast-icon"
                    />
                    <div className="forecast-temp">
                      <span className="temp-high">{convertTemp(day.temp_max)}°</span>
                      <span className="temp-separator">/</span>
                      <span className="temp-low">{convertTemp(day.temp_min)}°</span>
                    </div>
                    <p className="forecast-desc">{day.description}</p>
                  </div>
                ))}
              </div>
            </div>

            {/* Informations supplémentaires */}
            <div className="weather-card details-section">
              <h3 className="section-title">Détails supplémentaires</h3>
              <div className="details-grid">
                <div className="detail-card">
                  <div className="detail-item">
                    <span className="detail-label">Pression atmosphérique</span>
                    <span className="detail-value">{weather.pressure} hPa</span>
                  </div>
                </div>
                <div className="detail-card">
                  <div className="detail-item">
                    <span className="detail-label">Visibilité</span>
                    <span className="detail-value">{weather.visibility} km</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default Weather;
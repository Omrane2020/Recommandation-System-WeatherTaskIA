import { useState, useEffect } from "react";
import './SmartTaskWeather.css';
import { useNavigate } from "react-router-dom";

interface Activity {
  id: number;
  title: string;
  icon: string;
  weatherTags: string[];
  category?: string;
  description?: string;
}

interface Recommendation {
  score: number;
  recommendation: string;
  details?: {
    temperature?: number;
    conditions?: string;
    humidity?: number;
    windSpeed?: number;
    uvIndex?: number;
  };
}

const DEFAULT_ACTIVITIES: Activity[] = [
  {
    id: 1,
    title: "🎯 Randonnée",
    icon: "🎯",
    weatherTags: ["outdoor", "sun-critical"],
    category: "sport",
    description: "Randonnée en pleine nature"
  },
  {
    id: 2,
    title: "🚴 Cyclisme",
    icon: "🚴",
    weatherTags: ["outdoor", "wind-sensitive"],
    category: "sport",
    description: "Balade à vélo"
  },
  {
    id: 3,
    title: "🏃 Running",
    icon: "🏃",
    weatherTags: ["outdoor", "rain-prohibited"],
    category: "sport",
    description: "Course à pied"
  },
  {
    id: 4,
    title: "🧺 Pique-nique",
    icon: "🧺",
    weatherTags: ["outdoor", "rain-prohibited"],
    category: "loisir",
    description: "Repas en plein air"
  },
  {
    id: 5,
    title: "📸 Photographie",
    icon: "📸",
    weatherTags: ["outdoor", "sun-critical"],
    category: "loisir",
    description: "Séance photo"
  }
];

// Fonction utilitaire pour mapper les tags d'affichage
const getDisplayTag = (tag: string): string => {
  const tagMapping: { [key: string]: string } = {
    "outdoor": "extérieur",
    "indoor": "intérieur",
    "sun-critical": "ensoleillement critique",
    "wind-sensitive": "sensible au vent",
    "rain-prohibited": "pluie interdite",
    "temp-min": "température minimale",
    "temp-max": "température maximale"
  };
  return tagMapping[tag] || tag;
};

// Fonction pour formater les conditions météo
const formatWeatherCondition = (condition: string): string => {
  const conditionMap: { [key: string]: string } = {
    "clear sky": "Ciel dégagé",
    "few clouds": "Quelques nuages",
    "scattered clouds": "Nuages épars",
    "broken clouds": "Nuages fragmentés",
    "overcast clouds": "Ciel couvert",
    "mist": "Brume",
    "fog": "Brouillard",
    "light rain": "Pluie légère",
    "moderate rain": "Pluie modérée",
    "heavy rain": "Forte pluie",
    "thunderstorm": "Orage",
    "snow": "Neige",
    "shower rain": "Averses",
    "rain": "Pluie",
    "drizzle": "Bruine"
  };
  return conditionMap[condition.toLowerCase()] || condition;
};

const SmartTaskWeather = () => {
  const [city, setCity] = useState("");
  const [date, setDate] = useState("");
  const [activityId, setActivityId] = useState<number | null>(null);
  const [activities, setActivities] = useState<Activity[]>([]);
  const [recommendation, setRecommendation] = useState<Recommendation | null>(null);
  const [loading, setLoading] = useState(false);
  const [activitiesLoading, setActivitiesLoading] = useState(true);
  const [error, setError] = useState("");
  const [activitiesError, setActivitiesError] = useState("");
  const [searchQuery, setSearchQuery] = useState("");
  const [selectedCategory, setSelectedCategory] = useState<string>("all");
  const [hasSearched, setHasSearched] = useState(false); // Nouvel état pour suivre si une recherche a été faite
  const navigate = useNavigate();

  const handleGoToTWeather = () => {
    navigate("/");
  };

  // Chargement des activités
  useEffect(() => {
    const loadActivities = async () => {
      try {
        setActivitiesLoading(true);
        setActivitiesError("");

        const response = await fetch("http://localhost:8080/api/activities");

        if (!response.ok) {
          throw new Error(`Erreur HTTP: ${response.status}`);
        }

        const data = await response.json();

        // Transformation des données de l'API pour correspondre à notre interface
        const transformedActivities: Activity[] = data.map((item: any, index: number) => ({
          id: index + 1, // Génération d'un ID simple
          title: item.title,
          icon: item.icon,
          weatherTags: item.tags, // 'tags' devient 'weatherTags'
          // category et description ne sont pas fournis par l'API, donc optionnels
        }));

        setActivities(transformedActivities);
      } catch (err) {
        console.error("Erreur chargement activités:", err);
        setActivitiesError("Impossible de charger les activités depuis le serveur. Utilisation des activités par défaut.");
        setActivities(DEFAULT_ACTIVITIES);
      } finally {
        setActivitiesLoading(false);
      }
    };

    loadActivities();
  }, []);

  // Obtenir l'activité sélectionnée
  const getSelectedActivity = () => {
    return activities.find(a => a.id === activityId);
  };

  // Mettre à jour la recherche quand une activité est sélectionnée
  useEffect(() => {
    const selectedActivity = getSelectedActivity();
    if (selectedActivity) {
      setSearchQuery(selectedActivity.title);
    }
  }, [activityId]);

  // Filtrer les activités
  const filteredActivities = activities.filter(activity => {
    const matchesSearch = activity.title.toLowerCase().includes(searchQuery.toLowerCase());
    const matchesCategory = selectedCategory === "all";
    return matchesSearch && matchesCategory;
  });

  // Catégories simplifiées - puisque l'API n'en fournit pas
  const categories = ["all"];

  const retryLoadActivities = async () => {
    setActivitiesError("");
    setActivitiesLoading(true);

    try {
      const response = await fetch("http://localhost:8080/api/activities");
      if (!response.ok) throw new Error(`Erreur HTTP: ${response.status}`);

      const data = await response.json();

      // Même transformation que dans le chargement initial
      const transformedActivities: Activity[] = data.map((item: any, index: number) => ({
        id: index + 1,
        title: item.title,
        icon: item.icon,
        weatherTags: item.tags,
      }));

      setActivities(transformedActivities);
      setActivitiesError("");
    } catch (err) {
      console.error("Erreur lors de la reconnexion:", err);
      setActivitiesError("Impossible de se connecter au serveur. Les activités par défaut restent disponibles.");
      if (activities.length === 0) {
        setActivities(DEFAULT_ACTIVITIES);
      }
    } finally {
      setActivitiesLoading(false);
    }
  };

  const checkWeather = async () => {
    if (!city.trim() || !date || !activityId) {
      setError("Veuillez remplir tous les champs");
      return;
    }

    setLoading(true);
    setError("");
    setHasSearched(true); // Marquer qu'une recherche a été effectuée

    try {
      const response = await fetch("http://localhost:8081/api/tasks/check-weather", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          "Accept": "application/json",
        },
        body: JSON.stringify({ city: city.trim(), date, activityId }),
      });

      if (!response.ok) {
        throw new Error(`Erreur HTTP: ${response.status}`);
      }

      const data = await response.json();
      console.log("Données reçues de l'API:", data);

      // CORRECTION : Accéder aux données via data.recommendation
      const normalizedRecommendation: Recommendation = {
        score: data.recommendation?.score || 0,
        recommendation: data.recommendation?.recommendation || "Aucune recommandation disponible",
        details: data.recommendation?.details || {}
      };

      setRecommendation(normalizedRecommendation);

    } catch (err) {
      console.error("Erreur vérification météo:", err);
      setError(err instanceof Error ? err.message : "Erreur inconnue lors de la vérification météo");
    } finally {
      setLoading(false);
    }
  };

  const getRecommendationClass = (score: number) => {
    if (score >= 80) return "recommendation-perfect";
    if (score >= 60) return "recommendation-good";
    if (score >= 40) return "recommendation-warning";
    return "recommendation-danger";
  };

  const getStatusText = (score: number) => {
    if (score >= 80) return "Parfait";
    if (score >= 60) return "Bon";
    if (score >= 40) return "Défavorable";
    return "Critique";
  };

  const getStatusClass = (score: number) => {
    if (score >= 80) return "status-perfect";
    if (score >= 60) return "status-good";
    if (score >= 40) return "status-warning";
    return "status-danger";
  };

  const getTagIcon = (tag: string) => {
    const icons: { [key: string]: string } = {
      "outdoor": "🌳",
      "indoor": "🏠",
      "sun-critical": "☀️",
      "wind-sensitive": "💨",
      "rain-prohibited": "🌧️",
      "temp-min": "🌡️↓",
      "temp-max": "🌡️↑"
    };
    return icons[tag] || "🔹";
  };

  const getTagClass = (tag: string) => {
    const classes: { [key: string]: string } = {
      "outdoor": "tag-outdoor",
      "indoor": "tag-indoor",
      "sun-critical": "tag-sun",
      "wind-sensitive": "tag-wind",
      "rain-prohibited": "tag-rain",
      "temp-min": "tag-temp",
      "temp-max": "tag-temp"
    };
    return classes[tag] || "tag-default";
  };

  const resetForm = () => {
    setCity("");
    setDate("");
    setActivityId(null);
    setRecommendation(null);
    setError("");
    setSearchQuery("");
    setSelectedCategory("all");
    setHasSearched(false); // Réinitialiser l'état de recherche
  };

  // Gérer le changement de recherche
  const handleSearchChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const value = e.target.value;
    setSearchQuery(value);
    
    // Si l'utilisateur efface la recherche, désélectionner l'activité
    if (value === "" && activityId) {
      setActivityId(null);
    }
  };

  // Gérer la sélection d'activité
  const handleActivitySelect = (activity: Activity) => {
    setActivityId(activity.id);
    setError("");
  };

  return (
    <div className="smarttask-weather-page">
      {/* Header fixe */}
      <header className="weather-page-header">
        <div className="header-content">
          <div className="title-section">
            <h1 className="weather-wizard-title">
              <span className="emoji-weather">🌤️</span>
              SmartTask Météo
            </h1>
            <p className="weather-wizard-subtitle">
              Planifiez intelligemment selon la météo
            </p>
          </div>
          <button
            className="task-button"
            onClick={() => handleGoToTWeather()}
          >
            Weather
          </button>
          <button
            onClick={resetForm}
            className="reset-button"
            title="Réinitialiser le formulaire"
          >
            🔄 Nouvelle recherche
          </button>
        </div>
      </header>

      {/* Contenu principal */}
      <main className="weather-page-main">
        <div className="page-layout">
          {/* Colonne de gauche - Formulaire */}
          <div className="form-column">
            {/* Messages d'erreur */}
            {activitiesError && (
              <div className="error-container">
                <div className="error-header">
                  <span className="error-icon">⚠️</span>
                  <div className="error-title">Attention</div>
                </div>
                <div className="error-message">{activitiesError}</div>
                <div className="error-suggestion">
                  Les activités par défaut sont chargées. La fonctionnalité reste disponible.
                </div>
                <button
                  onClick={retryLoadActivities}
                  disabled={activitiesLoading}
                  className="retry-button"
                >
                  {activitiesLoading ? (
                    <>
                      <div className="loading-spinner"></div>
                      Reconnexion...
                    </>
                  ) : (
                    "🔄 Réessayer la connexion"
                  )}
                </button>
              </div>
            )}

            {/* Formulaire principal */}
            <div className="weather-form-card">
              <div className="form-section">
                <h3 className="form-section-title">📍 Informations de base</h3>
                <div className="form-grid">
                  <div className="form-group">
                    <label className="form-label">
                      <span className="label-icon">🏙️</span>
                      Lieu
                    </label>
                    <input
                      type="text"
                      placeholder="Ex: Paris, Lyon, Marseille..."
                      value={city}
                      onChange={(e) => {
                        setCity(e.target.value);
                        setError("");
                      }}
                      className="form-input"
                    />
                  </div>

                  <div className="form-group">
                    <label className="form-label">
                      <span className="label-icon">📅</span>
                      Date
                    </label>
                    <input
                      type="date"
                      value={date}
                      onChange={(e) => {
                        setDate(e.target.value);
                        setError("");
                      }}
                      className="form-input"
                      min={new Date().toISOString().split('T')[0]}
                    />
                  </div>
                </div>
              </div>

              {/* Sélection d'activité */}
              <div className="form-section">
                <h3 className="form-section-title">🎯 Choisir une activité</h3>

                {/* Barre de recherche et filtres */}
                <div className="activity-filters">
                  <div className="search-container">
                    <input
                      type="text"
                      placeholder="Rechercher une activité..."
                      value={searchQuery}
                      onChange={handleSearchChange}
                      className="search-input"
                    />
                    <span className="search-icon">🔍</span>
                  </div>

                  {/* Filtres de catégorie */}
                  <div className="category-filters">
                    {categories.map((category) => (
                      <button
                        key={category}
                        className={`category-filter ${selectedCategory === category ? "active" : ""}`}
                        onClick={() => setSelectedCategory(category)}
                      >
                        {category === "all" ? "Toutes" : category}
                      </button>
                    ))}
                  </div>
                </div>

                {/* Indicateur d'activité sélectionnée */}
                {getSelectedActivity() && (
                  <div className="selected-activity-indicator">
                    <span className="selected-label">Activité sélectionnée :</span>
                    <span className="selected-activity-name">
                      {getSelectedActivity()!.icon} {getSelectedActivity()!.title}
                    </span>
                  </div>
                )}

                {/* Liste des activités */}
                <div className="activities-container">
                  <div className="activities-grid">
                    {filteredActivities.map((activity) => (
                      <div
                        key={activity.id}
                        className={`activity-card ${activityId === activity.id ? "selected" : ""}`}
                        onClick={() => handleActivitySelect(activity)}
                      >
                        <div className="activity-header">
                          <h4 className="activity-name">
                            {activity.icon} {activity.title}
                          </h4>
                        </div>
                        <div className="activity-tags">
                          {activity.weatherTags.map((tag) => (
                            <span key={tag} className={`weather-tag ${getTagClass(tag)}`}>
                              {getTagIcon(tag)} {getDisplayTag(tag)}
                            </span>
                          ))}
                        </div>
                      </div>
                    ))}
                  </div>

                  {activitiesLoading && (
                    <div className="loading-message">
                      <div className="loading-spinner"></div>
                      Chargement des activités...
                    </div>
                  )}

                  {!activitiesLoading && filteredActivities.length === 0 && (
                    <div className="no-activities-message">
                      <span className="no-results-icon">🔍</span>
                      <div>
                        <strong>Aucune activité trouvée</strong>
                        <p>Essayez de modifier vos critères de recherche.</p>
                      </div>
                    </div>
                  )}
                </div>
              </div>

              {/* Message d'erreur général */}
              {error && (
                <div className="error-container">
                  <div className="error-header">
                    <span className="error-icon">❌</span>
                    <div className="error-title">Erreur</div>
                  </div>
                  <div className="error-message">{error}</div>
                </div>
              )}

              {/* Boutons d'action */}
              <div className="action-section">
                <div className="action-buttons">
                  <button
                    onClick={checkWeather}
                    disabled={
                      loading ||
                      activitiesLoading ||
                      activities.length === 0 ||
                      !activityId ||
                      !city.trim() ||
                      !date
                    }
                    className="weather-check-button"
                  >
                    {loading ? (
                      <>
                        <div className="loading-spinner"></div>
                        Analyse en cours...
                      </>
                    ) : (
                      <>
                        <span className="button-icon">🔍</span>
                        Vérifier la météo
                      </>
                    )}
                  </button>

                  <button onClick={resetForm} className="secondary-button">
                    🗑️ Effacer
                  </button>
                </div>
              </div>
            </div>
          </div>

          {/* Colonne de droite - Résultats */}
          <div className={`recommendation-card ${!hasSearched ? 'initial-state' : ''}`}>
            {!hasSearched ? (
              // État initial - Affichage avec soleil
              <div className="initial-recommendation">
                <div className="sun-background">
                  <div className="sun-icon">☀️</div>
                </div>
                <h2 className="initial-title">Prêt à planifier ?</h2>
                <p className="initial-message">
                  Remplissez le formulaire et cliquez sur "Vérifier la météo" pour obtenir des recommandations personnalisées selon vos activités.
                </p>
                <div className="initial-tips">
                  <div className="tip">
                    <span className="tip-icon">📍</span>
                    <span>Choisissez un lieu</span>
                  </div>
                  <div className="tip">
                    <span className="tip-icon">📅</span>
                    <span>Sélectionnez une date</span>
                  </div>
                  <div className="tip">
                    <span className="tip-icon">🎯</span>
                    <span>Pick an activity</span>
                  </div>
                </div>
              </div>
            ) : (
              // État après recherche - Affichage des résultats
              <>
                <div className="recommendation-header">
                  <div className="recommendation-title-section">
                    <h2 className="recommendation-title">📊 Recommandation Météo</h2>

                    {recommendation && (
                      <span className={`status-indicator ${getStatusClass(recommendation.score)}`}>
                        {getStatusText(recommendation.score)}
                      </span>
                    )}
                  </div>

                  <div className="recommendation-score">
                    {recommendation?.score ?? "N/A"}%
                  </div>
                </div>

                {/* Barre de progression */}
                <div className="score-bar">
                  <div
                    className="score-fill"
                    style={{ width: `${recommendation?.score ?? 0}%` }}
                  ></div>
                </div>

                {/* Message */}
                <div className="recommendation-message">
                  {recommendation?.recommendation ?? "Aucune recommandation disponible."}
                </div>

                {/* Détails météo */}
                <div className="weather-details">
                  <p>🌡️ <strong>Température :</strong> {recommendation?.details?.temperature?.toFixed?.(1) ?? "N/A"}°C</p>
                  <p>☁️ <strong>Conditions :</strong> {formatWeatherCondition(recommendation?.details?.conditions ?? "N/A")}</p>
                  <p>💧 <strong>Humidité :</strong> {recommendation?.details?.humidity ?? "N/A"}%</p>
                  <p>💨 <strong>Vent :</strong>
                    {recommendation?.details?.windSpeed
                      ? (recommendation.details.windSpeed * 3.6).toFixed(1)
                      : "N/A"} km/h
                  </p>
                  <p>☀️ <strong>Index UV :</strong> {recommendation?.details?.uvIndex ?? "N/A"}</p>
                </div>

                {/* Tags de l'activité */}
                {getSelectedActivity() && (
                  <div className="selected-activity-tags">
                    <h4>Caractéristiques de l'activité :</h4>

                    <div className="tags-container">
                      {getSelectedActivity()!.weatherTags.map((tag) => (
                        <span key={tag} className={`activity-tag ${getTagClass(tag)}`}>
                          {getTagIcon(tag)} {getDisplayTag(tag)}
                        </span>
                      ))}
                    </div>
                  </div>
                )}
              </>
            )}
          </div>
        </div>
      </main>
    </div>
  );
};

export default SmartTaskWeather;
/*
import Weather from "./Weather";

function App() {
  return (
    <div className="min-h-screen bg-gray-50">
      <Weather />
    </div>
  );
}

export default App;
*/
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import SmartTaskWeather from './pages/SmartTaskWeather';
import Weather from './Weather';

function App() {
  return (
    <Router>
      <div className="min-h-screen bg-gray-50">
        <Routes>
          <Route path="/" element={<Weather />} />
          <Route path="/task" element={<SmartTaskWeather />} />
        </Routes>
      </div>
    </Router>
  );
}
export default App;

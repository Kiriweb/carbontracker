import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import AdminApprovalPage from './pages/AdminApprovalPage';
import DashboardPage from './pages/DashboardPage';
import AdminDashboardPage from "./pages/AdminDashboardPage";

const App = () => {
  return (
    <Router>
      <Routes>
        <Route path="/" element={<Navigate to="/login" />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
		<Route path="/admin" element={<AdminApprovalPage />} />
		<Route path="/dashboard" element={<DashboardPage />} />
		<Route path="/admin/dashboard" element={<AdminDashboardPage />} />
      </Routes>
    </Router>
  );
};

export default App;

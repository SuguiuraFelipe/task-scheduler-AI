import React from 'react';
import { BrowserRouter as Router, Navigate, Outlet, Routes, Route } from 'react-router-dom';
import { authService } from './services/authService';
import Navbar from './components/Navbar';
import Dashboard from './pages/Dashboard';
import ConversationsPage from './pages/ConversationsPage';
import LoginPage from './pages/LoginPage';
import ProtectedRoute from './components/ProtectedRoute';

function AppShell() {
  const currentUser = authService.getCurrentUser();

  return (
    <>
      <Navbar userId={currentUser.userId} />
      <div style={{ minHeight: '100vh', background: 'var(--bg)' }}>
        <Outlet />
      </div>
    </>
  );
}

function App() {
  return (
    <Router>
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route
          element={(
            <ProtectedRoute>
              <AppShell />
            </ProtectedRoute>
          )}
        >
          <Route path="/" element={<Dashboard />} />
          <Route path="/conversations" element={<ConversationsPage />} />
        </Route>
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </Router>
  );
}

export default App;

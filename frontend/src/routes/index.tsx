import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import { ProtectedRoute, PublicRoute } from './RouteGuards';
import LoginPage from '../features/auth/pages/LoginPage';
import { useAuth } from '../context/AuthContext';

/**
 * Main Application Routes component.
 * Defines the navigation tree and applies security guards to specific paths.
 * 
 * @author L.F. Desenvolvimento de Softwares LTDA
 */
const AppRoutes: React.FC = () => {
  const { user } = useAuth();

  return (
    <Routes>
      {/* Public Routes: Accessible only when NOT authenticated */}
      <Route 
        path="/login" 
        element={
          <PublicRoute>
            <LoginPage />
          </PublicRoute>
        } 
      />

      {/* Protected Routes: Accessible only when authenticated */}
      <Route 
        path="/" 
        element={
          <ProtectedRoute>
            <div style={{ padding: '24px' }}>
              <h1>Dashboard Placeholder</h1>
              <p>Welcome, {user?.fullName}!</p>
              <p>Role: {user?.globalRole}</p>
              <button onClick={() => window.location.reload()}>Reload to test Auth State</button>
            </div>
          </ProtectedRoute>
        } 
      />

      {/* Fallback: Redirect any unknown route to home */}
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
};

export default AppRoutes;
import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import { ProtectedRoute, PublicRoute } from './RouteGuards';
import MainLayout from '../layout/MainLayout';
import LoginPage from '../features/auth/pages/LoginPage';
import { useAuth } from '../context/AuthContext';

/**
 * Main Application Routes component.
 * Organizes the routing tree using nested routes for the main layout
 * and applies security guards to prevent unauthorized access.
 * 
 * @author L.F. Desenvolvimento de Softwares LTDA
 */
const AppRoutes: React.FC = () => {
  const { user } = useAuth();

  return (
    <Routes>
      {/* 1. Public Routes: Accessible only when NOT authenticated */}
      <Route 
        path="/login" 
        element={
          <PublicRoute>
            <LoginPage />
          </PublicRoute>
        } 
      />

      {/* 2. Protected Routes: Accessible only when authenticated */}
      {/* The MainLayout acts as a parent for all internal pages */}
      <Route 
        element={
          <ProtectedRoute>
            <MainLayout />
          </ProtectedRoute>
        }
      >
        {/* Dashboard / Home */}
        <Route 
          path="/" 
          element={
            <div style={{ textAlign: 'center', paddingTop: '50px' }}>
              <h2>Welcome back, {user?.fullName}!</h2>
              <p>You are logged in as a <strong>{user?.globalRole}</strong>.</p>
            </div>
          } 
        />

        {/* User Management Placeholder */}
        <Route 
          path="/users" 
          element={
            <div>
              <h3>User Management</h3>
              <p>The user list and management features will be implemented here.</p>
            </div>
          } 
        />

        {/* Languages Placeholder */}
        <Route 
          path="/languages" 
          element={
            <div>
              <h3>Language Settings</h3>
              <p>System localization and language CRUD will be implemented here.</p>
            </div>
          } 
        />
      </Route>

      {/* 3. Fallback: Redirect any unknown route to home */}
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
};

export default AppRoutes;
import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import { ProtectedRoute, PublicRoute } from './RouteGuards';
import MainLayout from '../layout/MainLayout';
import LoginPage from '../features/auth/pages/LoginPage';
import UserListPage from '../features/users/pages/UserListPage';
import { useAuth } from '../context/AuthContext';

/**
 * Main Application Routes component.
 * 
 * Defines the navigation tree of the Orquestro platform.
 * It manages the transition between the public authentication area and the 
 * protected administrative area, applying the appropriate route guards.
 * 
 * @author L.F. Desenvolvimento de Softwares LTDA
 */
const AppRoutes: React.FC = () => {
  const { user } = useAuth();

  return (
    <Routes>
      {/* 
          1. Public Routes Area 
          Routes accessible only to users who are NOT authenticated.
          If an authenticated user tries to access /login, the PublicRoute 
          guard will redirect them to the home page.
      */}
      <Route 
        path="/login" 
        element={
          <PublicRoute>
            <LoginPage />
          </PublicRoute>
        } 
      />

      {/* 
          2. Protected Application Shell 
          The MainLayout acts as a master wrapper for all internal pages.
          The ProtectedRoute guard ensures the entire tree is inaccessible 
          without a valid session.
      */}
      <Route 
        element={
          <ProtectedRoute>
            <MainLayout />
          </ProtectedRoute>
        }
      >
        {/* Dashboard / Home View */}
        <Route 
          path="/" 
          element={
            <div style={{ textAlign: 'center', paddingTop: '50px' }}>
              <h2>Welcome back, {user?.fullName}!</h2>
              <p>You are logged in as a <strong>{user?.globalRole}</strong>.</p>
            </div>
          } 
        />

        {/* User Management Module */}
        <Route 
          path="/users" 
          element={<UserListPage />} 
        />

        {/* Languages Settings Placeholder 
            Note: This will be replaced by the LanguageListPage in the next steps.
        */}
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

      {/* 
          3. Global Fallback 
          Captures any undefined URLs and redirects the user to the home page,
          maintaining application consistency.
      */}
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
};

export default AppRoutes;
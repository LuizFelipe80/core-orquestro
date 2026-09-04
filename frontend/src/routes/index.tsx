import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import { ProtectedRoute, PublicRoute } from './RouteGuards';
import MainLayout from '../layout/MainLayout';
import LoginPage from '../features/auth/pages/LoginPage';
import UserListPage from '../features/users/pages/UserListPage';
import ProfilePage from '../features/users/pages/ProfilePage';
import LanguageListPage from '../features/languages/pages/LanguageListPage';
import ModuleRoleCatalogPage from '../features/access-control/pages/ModuleRoleCatalogPage';
import UserRoleManagementPage from '../features/access-control/pages/UserRoleManagementPage';
import { useAuth } from '../context/AuthContext';
import { Color } from 'antd/es/color-picker';

/**
 * Main Application Routes component.
 * 
 * Orchestrates the navigation tree, merging core identity features with 
 * the new indirect RBAC governance modules.
 * 
 * @author L.F. Desenvolvimento de Softwares LTDA
 */
const AppRoutes: React.FC = () => {
  const { user } = useAuth();

  return (
    <Routes>
      {/* 1. Public Routes Area */}
      <Route 
        path="/login" 
        element={
          <PublicRoute>
            <LoginPage />
          </PublicRoute>
        } 
      />

      {/* 2. Protected Application Shell */}
      <Route 
        element={
          <ProtectedRoute>
            <MainLayout />
          </ProtectedRoute>
        }
      >
        {/* Dashboard / Home View - Preserving your original implementation */}
        <Route 
          path="/" 
          element={
            <div style={{ textAlign: 'center', paddingTop: '50px' }}>
              <h2 style={{fontWeight: '400', color: '#333' }}>Welcome back, <strong> {user?.fullName}! </strong> </h2>
              <p>You are logged in as: <strong>{user?.roles.join(', ')}</strong>.</p>
            </div>
          } 
        />

        {/* Identity & Localization */}
        <Route path="/users" element={<UserListPage />} />
        <Route path="/languages" element={<LanguageListPage />} />
        <Route path="/profile" element={<ProfilePage />} />

        {/* 
            Access Control & Governance (New Features)
            Routes for managing the Indirect RBAC model.
        */}
        <Route path="/user-roles" element={<UserRoleManagementPage />} />
        <Route path="/module-roles" element={<ModuleRoleCatalogPage />} />
      </Route>

      {/* 3. Global Fallback */}
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
};

export default AppRoutes;
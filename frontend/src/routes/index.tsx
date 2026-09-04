import React, { Suspense, lazy } from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import { Spin } from 'antd';
import { ProtectedRoute, PublicRoute } from './RouteGuards';
import MainLayout from '../layout/MainLayout';
import { useAuth } from '../context/AuthContext';

/* Lazy loaded pages for optimized bundle size and faster initial load (Code-Splitting) */
const LoginPage = lazy(() => import('../features/auth/pages/LoginPage'));
const UserListPage = lazy(() => import('../features/users/pages/UserListPage'));
const ProfilePage = lazy(() => import('../features/users/pages/ProfilePage'));
const LanguageListPage = lazy(() => import('../features/languages/pages/LanguageListPage'));
const ModuleRoleCatalogPage = lazy(() => import('../features/access-control/pages/ModuleRoleCatalogPage'));
const UserRoleManagementPage = lazy(() => import('../features/access-control/pages/UserRoleManagementPage'));

const PageLoader: React.FC = () => (
  <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: 280, height: '100%' }}>
    <Spin size="large" />
  </div>
);

/**
 * Main Application Routes component.
 * 
 * Orchestrates the navigation tree, merging core identity features with 
 * the indirect RBAC governance modules with optimized lazy loading.
 * 
 * @author L.F. Desenvolvimento de Softwares LTDA
 */
const AppRoutes: React.FC = () => {
  const { user } = useAuth();

  return (
    <Suspense fallback={<PageLoader />}>
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
          {/* Dashboard / Home View */}
          <Route 
            path="/" 
            element={
              <div style={{ textAlign: 'center', paddingTop: '50px' }}>
                <h2 style={{ fontWeight: '400', color: '#333' }}>Welcome back, <strong> {user?.fullName}! </strong> </h2>
                <p>You are logged in as: <strong>{user?.roles.join(', ')}</strong>.</p>
              </div>
            } 
          />

          {/* Identity & Localization */}
          <Route path="/users" element={<UserListPage />} />
          <Route path="/languages" element={<LanguageListPage />} />
          <Route path="/profile" element={<ProfilePage />} />

          {/* Access Control & Governance */}
          <Route path="/user-roles" element={<UserRoleManagementPage />} />
          <Route path="/module-roles" element={<ModuleRoleCatalogPage />} />
        </Route>

        {/* 3. Global Fallback */}
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </Suspense>
  );
};

export default AppRoutes;
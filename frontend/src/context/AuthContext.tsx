import React, { createContext, useContext, useState, useEffect, useCallback } from 'react';
import api from '../api/axios';

/**
 * Interface representing the user data stored in the authentication state.
 * Updated to support multiple roles as part of the indirect RBAC model.
 */
interface User {
  id: string;
  email: string;
  fullName: string;
  roles: string[];
}

/**
 * Interface for the sign-in response from the backend.
 * Matches the updated AuthenticationResponseDTO.
 */
interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  userId: string;
  email: string;
  fullName: string;
  roles: string[];
}

/**
 * Defines the shape of the Authentication Context.
 */
interface AuthContextData {
  user: User | null;
  loading: boolean;
  signIn(credentials: object): Promise<void>;
  signOut(): void;
  isAuthenticated: boolean;
  hasRole(roleName: string): boolean;
}

const AuthContext = createContext<AuthContextData>({} as AuthContextData);

/**
 * Provider component that manages the global authentication state.
 * Handles persistence, token storage, and session lifecycle.
 * 
 * @param children React components to be wrapped.
 * @author L.F. Desenvolvimento de Softwares LTDA
 */
export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);

  /**
   * Loads the session data from local storage on application startup.
   */
  useEffect(() => {
    const loadStorageData = () => {
      const storedUser = localStorage.getItem('@Orquestro:user');
      const storedToken = localStorage.getItem('@Orquestro:accessToken');

      if (storedUser && storedToken) {
        setUser(JSON.parse(storedUser));
      }
      setLoading(false);
    };

    loadStorageData();
  }, []);

  /**
   * Authenticates the user and initializes the secure session.
   * 
   * @param credentials The user's login data.
   */
  const signIn = useCallback(async (credentials: object) => {
    const response = await api.post<AuthResponse>('/auth/authenticate', credentials);
    const { accessToken, refreshToken, ...userData } = response.data;

    const userToSave: User = {
      id: userData.userId,
      email: userData.email,
      fullName: userData.fullName,
      roles: userData.roles,
    };

    localStorage.setItem('@Orquestro:accessToken', accessToken);
    localStorage.setItem('@Orquestro:refreshToken', refreshToken);
    localStorage.setItem('@Orquestro:user', JSON.stringify(userToSave));

    setUser(userToSave);
  }, []);

  /**
   * Clears the current session and notifies the backend.
   */
  const signOut = useCallback(() => {
    const refreshToken = localStorage.getItem('@Orquestro:refreshToken');
    
    if (refreshToken) {
      api.post('/auth/logout', { refreshToken }).catch(() => {
        console.warn('Logout notification to backend failed, proceeding with local cleanup.');
      });
    }

    localStorage.removeItem('@Orquestro:accessToken');
    localStorage.removeItem('@Orquestro:refreshToken');
    localStorage.removeItem('@Orquestro:user');
    setUser(null);
  }, []);

  /**
   * Helper method to check if the current user possesses a specific role.
   * 
   * @param roleName The name of the role to check (e.g., 'ADMINISTRATOR').
   * @returns true if the user has the role assigned.
   */
  const hasRole = useCallback((roleName: string) => {
    return user?.roles.includes(roleName) || false;
  }, [user]);

  return (
    <AuthContext.Provider value={{ 
      user, 
      loading, 
      signIn, 
      signOut, 
      isAuthenticated: !!user,
      hasRole
    }}>
      {children}
    </AuthContext.Provider>
  );
};

/**
 * Hook to access the authentication state and methods.
 */
export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
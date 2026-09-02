import React, { createContext, useContext, useState, useEffect, useCallback } from 'react';
import api from '../api/axios';

/**
 * Interface representing the user data stored in the authentication state.
 */
interface User {
  id: string;
  email: string;
  fullName: string;
  globalRole: 'ROLE_ADMIN' | 'ROLE_MANAGER' | 'ROLE_USER';
}

/**
 * Interface for the sign-in response from the backend.
 */
interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  userId: string;
  email: string;
  fullName: string;
  globalRole: string;
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
}

const AuthContext = createContext<AuthContextData>({} as AuthContextData);

/**
 * Provider component that wraps the application to provide authentication state.
 * It handles the initial token check and defines sign-in/sign-out logic.
 * 
 * @param children React components to be wrapped.
 */
export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);

  /**
   * Effect to check for existing credentials in localStorage upon application start.
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
   * Authenticates the user with the backend and stores the session tokens.
   * 
   * @param credentials Email and password object.
   */
  const signIn = useCallback(async (credentials: object) => {
    const response = await api.post<AuthResponse>('/auth/authenticate', credentials);
    const { accessToken, refreshToken, ...userData } = response.data;

    const userToSave: User = {
      id: userData.userId,
      email: userData.email,
      fullName: userData.fullName,
      globalRole: userData.globalRole as User['globalRole'],
    };

    localStorage.setItem('@Orquestro:accessToken', accessToken);
    localStorage.setItem('@Orquestro:refreshToken', refreshToken);
    localStorage.setItem('@Orquestro:user', JSON.stringify(userToSave));

    setUser(userToSave);
  }, []);

  /**
   * Clears the user session and removes tokens from storage.
   */
  const signOut = useCallback(() => {
    const refreshToken = localStorage.getItem('@Orquestro:refreshToken');
    
    // Attempt to notify backend of logout (fire and forget)
    if (refreshToken) {
      api.post('/auth/logout', { refreshToken }).catch(() => {});
    }

    localStorage.removeItem('@Orquestro:accessToken');
    localStorage.removeItem('@Orquestro:refreshToken');
    localStorage.removeItem('@Orquestro:user');
    setUser(null);
  }, []);

  return (
    <AuthContext.Provider value={{ 
      user, 
      loading, 
      signIn, 
      signOut, 
      isAuthenticated: !!user 
    }}>
      {children}
    </AuthContext.Provider>
  );
};

/**
 * Custom hook to easily access the authentication context.
 * 
 * @returns The authentication context data.
 */
export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
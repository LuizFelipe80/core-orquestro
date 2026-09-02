import axios, { AxiosError, InternalAxiosRequestConfig } from 'axios';

/**
 * Interface for the token refresh response.
 */
interface RefreshResponse {
  accessToken: string;
  refreshToken: string;
}

/**
 * Interface to manage the failed request queue during refresh.
 */
interface FailedRequest {
  onSuccess: (token: string) => void;
  onFailure: (error: AxiosError) => void;
}

let isRefreshing = false;
let failedRequestsQueue: FailedRequest[] = [];

/**
 * Global API instance configured with Vite environment variables.
 */
const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

/**
 * Request Interceptor:
 * Injects the JWT Access Token into the 'Authorization' header.
 */
api.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = localStorage.getItem('@Orquestro:accessToken');
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

/**
 * Response Interceptor:
 * Intercepts 401 errors to attempt a silent token refresh using the rotation mechanism.
 */
api.interceptors.response.use(
  (response) => response,
  (error: AxiosError) => {
    const { response, config } = error;
    const status = response?.status;

    /* If error is 401 and it's not a login request, try to refresh */
    if (status === 401 && config && !config.url?.includes('/auth/authenticate')) {
      
      /* If already refreshing, queue this request */
      if (isRefreshing) {
        return new Promise((resolve, reject) => {
          failedRequestsQueue.push({
            onSuccess: (token: string) => {
              if (config.headers) config.headers.Authorization = `Bearer ${token}`;
              resolve(api(config));
            },
            onFailure: (err: AxiosError) => {
              reject(err);
            },
          });
        });
      }

      isRefreshing = true;
      const refreshToken = localStorage.getItem('@Orquestro:refreshToken');

      /* If no refresh token exists, we cannot renew access */
      if (!refreshToken) {
        window.location.href = '/login';
        return Promise.reject(error);
      }

      return new Promise((resolve, reject) => {
        api.post<RefreshResponse>('/auth/refresh', { refreshToken })
          .then((res) => {
            const { accessToken: newAccessToken, refreshToken: newRefreshToken } = res.data;

            localStorage.setItem('@Orquestro:accessToken', newAccessToken);
            localStorage.setItem('@Orquestro:refreshToken', newRefreshToken);

            /* Update current request and re-run it */
            if (config.headers) config.headers.Authorization = `Bearer ${newAccessToken}`;
            
            /* Process all queued requests with the new token */
            failedRequestsQueue.forEach((request) => request.onSuccess(newAccessToken));
            failedRequestsQueue = [];

            resolve(api(config));
          })
          .catch((err) => {
            /* If refresh fails, clear everything and redirect to login */
            failedRequestsQueue.forEach((request) => request.onFailure(err));
            failedRequestsQueue = [];
            
            localStorage.removeItem('@Orquestro:accessToken');
            localStorage.removeItem('@Orquestro:refreshToken');
            localStorage.removeItem('@Orquestro:user');
            
            window.location.href = '/login';
            reject(err);
          })
          .finally(() => {
            isRefreshing = false;
          });
      });
    }

    return Promise.reject(error);
  }
);

export default api;
// src/config/environment.ts
/**
 * Environment configuration for the application
 * This allows switching between development and production environments
 */

interface EnvironmentConfig {
    /** Base URL for API requests */
    apiBaseUrl: string;

    /** Base URL for WebSocket connections */
    wsBaseUrl: string;

    /** Base URL for OAuth redirects */
    oauthRedirectBaseUrl: string;

    /** Frontend base URL */
    frontendBaseUrl: string;
}

// Development environment (localhost)
const devConfig: EnvironmentConfig = {
    apiBaseUrl: 'http://localhost:8080/api',
    wsBaseUrl: 'http://localhost:8080/ws-battle',
    oauthRedirectBaseUrl: 'http://localhost:8080/api/oauth2/authorize',
    frontendBaseUrl: 'http://localhost'
};

// Production environment (EC2)
const prodConfig: EnvironmentConfig = {
    apiBaseUrl: 'http://13.125.187.28:8080/api',
    wsBaseUrl: 'http://13.125.187.28:8080/ws-battle',
    oauthRedirectBaseUrl: 'http://13.125.187.28:8080/api/oauth2/authorize',
    frontendBaseUrl: 'http://13.125.187.28'
};

// Use production config when in production mode
const isProduction = import.meta.env.PROD || window.location.hostname !== 'localhost';
const config: EnvironmentConfig = isProduction ? prodConfig : devConfig;

export default config;
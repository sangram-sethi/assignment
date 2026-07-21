import { InjectionToken } from '@angular/core';

/**
 * Base URL for the NBC API. During development the Angular dev-server proxies
 * `/api` to the Spring Boot backend (see proxy.conf.json); in production the
 * built app is served by Spring so the same relative path resolves same-origin.
 */
export const API_BASE = new InjectionToken<string>('API_BASE', {
  providedIn: 'root',
  factory: () => '/api',
});

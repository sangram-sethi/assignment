import { ApplicationConfig, inject, provideAppInitializer, provideBrowserGlobalErrorListeners } from '@angular/core';
import { provideRouter } from '@angular/router';

import { routes } from './app.routes';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { authInterceptor } from './core/auth/auth.interceptor';
import { errorInterceptor } from './core/auth/error.interceptor';
import { AuthService } from './core/auth/auth.service';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes),
    // Order matters: authInterceptor sends credentials, errorInterceptor reacts to 401s.
    provideHttpClient(withInterceptors([authInterceptor, errorInterceptor])),
    // Restore the session (via the httpOnly cookie) before the app renders, so a
    // page reload doesn't log the user out.
    provideAppInitializer(() => inject(AuthService).hydrate())
  ]
};

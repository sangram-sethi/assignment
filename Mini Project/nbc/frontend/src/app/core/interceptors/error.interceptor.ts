import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { AuthService } from '../services/auth.service';
import { ToastService } from '../services/toast.service';
import { apiErrorMessage } from '../../shared/http-error';

/**
 * Centralises cross-cutting error handling: session expiry, connectivity and
 * server faults surface a toast; contextual 4xx errors are left for components.
 */
export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const toast = inject(ToastService);
  const isAuthCall = req.url.includes('/auth/');

  return next(req).pipe(
    catchError((err: HttpErrorResponse) => {
      if (err.status === 401 && !isAuthCall) {
        if (auth.isAuthenticated()) {
          toast.warning('Session expired', 'Please sign in again to continue.');
          auth.logout();
        }
      } else if (err.status === 0) {
        toast.error('Connection lost', 'We could not reach the server.');
      } else if (err.status >= 500) {
        toast.error('Server error', apiErrorMessage(err, 'An unexpected error occurred.'));
      }
      return throwError(() => err);
    }),
  );
};

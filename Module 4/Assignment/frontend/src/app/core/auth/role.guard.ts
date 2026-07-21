import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from './auth.service';

/**
 * Guards a route by role. Returns a CanActivateFn configured with the roles that
 * are allowed to enter. A user needs at least ONE of the given roles.
 *
 * Usage in a route:
 *   { path: 'admin', canActivate: [roleGuard('ROLE_ADMIN')], ... }
 *
 * Behaviour:
 *   - Not logged in            -> redirect to /login
 *   - Logged in, wrong role    -> redirect to /products (authorized landing page)
 *   - Logged in, correct role  -> allow
 */
export function roleGuard(...allowedRoles: string[]): CanActivateFn {
  return () => {
    const auth = inject(AuthService);
    const router = inject(Router);

    if (!auth.isLoggedIn()) {
      return router.createUrlTree(['/login']);
    }

    const userRoles = auth.roles();
    const permitted = allowedRoles.some((role) => userRoles.includes(role));

    return permitted ? true : router.createUrlTree(['/products']);
  };
}

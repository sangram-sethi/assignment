import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { Role } from '../models';

/** Blocks unauthenticated access, remembering the intended destination. */
export const authGuard: CanActivateFn = (_route, state) => {
  const auth = inject(AuthService);
  const router = inject(Router);
  if (auth.isAuthenticated()) return true;
  return router.createUrlTree(['/login'], { queryParams: { returnUrl: state.url } });
};

/** Restricts a route to specific role(s) declared via `data.roles`. */
export const roleGuard: CanActivateFn = (route) => {
  const auth = inject(AuthService);
  const router = inject(Router);
  const roles = (route.data['roles'] as Role[] | undefined) ?? [];
  const current = auth.role();
  if (current && (roles.length === 0 || roles.includes(current))) return true;
  return router.createUrlTree([auth.homeRoute()]);
};

/** Keeps authenticated users away from the auth screens. */
export const guestGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);
  if (!auth.isAuthenticated()) return true;
  return router.createUrlTree([auth.homeRoute()]);
};

/** Sends the index route to the correct landing page for the current role. */
export const homeGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);
  return router.createUrlTree([auth.homeRoute()]);
};

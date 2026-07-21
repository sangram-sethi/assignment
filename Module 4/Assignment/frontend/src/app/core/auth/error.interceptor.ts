import { HttpInterceptorFn } from "@angular/common/http";
import { inject } from "@angular/core";
import { Router } from "@angular/router";
import { catchError, throwError } from "rxjs";
import { AuthService } from "./auth.service";

/**
 * Reacts to authentication failures. A 401 means the token is missing, expired
 * or invalid, so we clear session state and send the user back to login. The
 * login request itself is excluded — its 401 simply means "bad credentials" and
 * is handled by the login component. The error is re-thrown so component-level
 * error handlers still run.
 */
export const errorInterceptor: HttpInterceptorFn = (req, next) => {
    const auth = inject(AuthService);
    const router = inject(Router);

    return next(req).pipe(
        catchError((err) => {
            // A 401 on a normal request means the cookie is missing/expired.
            // Exclude the auth probes: /login's 401 = bad credentials, and
            // /me's 401 during startup just means "not logged in yet".
            const isAuthProbe = req.url.includes('/auth/login') || req.url.includes('/auth/me');
            if (err.status === 401 && !isAuthProbe) {
                auth.clearSession();
                router.navigate(['/login']);
            }
            return throwError(() => err);
        })
    );
};

import { HttpInterceptorFn } from "@angular/common/http";

/**
 * Auth is carried by an httpOnly cookie the browser attaches automatically, so
 * we no longer set an Authorization header. We only enable `withCredentials`
 * so the cookie is included on cross-origin requests (e.g. the deployed API).
 */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
    return next(req.clone({ withCredentials: true }));
};
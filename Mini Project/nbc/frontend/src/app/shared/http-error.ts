import { HttpErrorResponse } from '@angular/common/http';
import { ApiError } from '../core/models';

/** Extracts a friendly, user-facing message from any HTTP error. */
export function apiErrorMessage(err: unknown, fallback = 'Something went wrong.'): string {
  if (err instanceof HttpErrorResponse) {
    if (err.status === 0) return 'Cannot reach the server. Please check your connection.';
    const body = err.error as ApiError | string | undefined;
    if (typeof body === 'string' && body.trim()) return body;
    if (body && typeof body === 'object') {
      const msg = (body as ApiError).message;
      if (Array.isArray(msg)) return msg.join(' · ');
      if (typeof msg === 'string' && msg.trim()) return msg;
      if ((body as ApiError).error) return (body as ApiError).error;
    }
    if (err.status === 401) return 'Invalid credentials.';
    if (err.status === 403) return 'You are not allowed to perform this action.';
    if (err.status === 404) return 'The requested resource was not found.';
  }
  return fallback;
}

/** Field-level validation messages (server returns an array for 400s). */
export function apiValidationMessages(err: unknown): string[] {
  if (err instanceof HttpErrorResponse) {
    const body = err.error as ApiError | undefined;
    if (body && Array.isArray(body.message)) return body.message;
  }
  return [];
}

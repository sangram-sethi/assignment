import { HttpErrorResponse } from '@angular/common/http';

/**
 * Translates a raw HTTP failure into a message that is safe and meaningful to
 * show a user. Centralising this keeps components consistent and avoids leaking
 * technical details. `status === 0` is the key case for "backend unreachable"
 * (server down, network error, CORS) — the browser never got a response.
 */
export function toUserMessage(err: unknown): string {
  if (err instanceof HttpErrorResponse) {
    if (err.status === 0) {
      return 'Cannot reach the server. Please check your connection and try again shortly.';
    }
    if (err.status === 401) {
      return 'Your session has expired. Please log in again.';
    }
    if (err.status === 403) {
      return 'You do not have permission to perform this action.';
    }
    if (err.status >= 500) {
      return 'The server ran into a problem. Please try again later.';
    }
    // Prefer the backend's own ApiError message when it provides one.
    if (typeof err.error?.message === 'string') {
      return err.error.message;
    }
  }
  return 'Something went wrong. Please try again.';
}

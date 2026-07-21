import { Injectable, signal } from '@angular/core';

export type ToastKind = 'success' | 'error' | 'info' | 'warning';

export interface Toast {
  id: number;
  kind: ToastKind;
  title: string;
  message?: string;
  duration: number;
}

@Injectable({ providedIn: 'root' })
export class ToastService {
  private seq = 0;
  readonly toasts = signal<Toast[]>([]);

  private push(kind: ToastKind, title: string, message?: string, duration = 4200): void {
    const id = ++this.seq;
    this.toasts.update((list) => [...list, { id, kind, title, message, duration }]);
    if (duration > 0) {
      setTimeout(() => this.dismiss(id), duration);
    }
  }

  success(title: string, message?: string): void {
    this.push('success', title, message);
  }
  error(title: string, message?: string): void {
    this.push('error', title, message, 6000);
  }
  info(title: string, message?: string): void {
    this.push('info', title, message);
  }
  warning(title: string, message?: string): void {
    this.push('warning', title, message);
  }

  dismiss(id: number): void {
    this.toasts.update((list) => list.filter((t) => t.id !== id));
  }
}

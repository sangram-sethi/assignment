import { Injectable, effect, signal } from '@angular/core';

const KEY = 'aurora-sidebar-collapsed';

/** Shared UI state for the authenticated shell (sidebar + mobile drawer). */
@Injectable({ providedIn: 'root' })
export class LayoutStore {
  readonly collapsed = signal<boolean>(this.restore());
  readonly mobileOpen = signal(false);

  constructor() {
    effect(() => {
      try {
        localStorage.setItem(KEY, this.collapsed() ? '1' : '0');
      } catch {
        /* ignore */
      }
    });
  }

  toggleCollapsed(): void {
    this.collapsed.update((v) => !v);
  }
  toggleMobile(): void {
    this.mobileOpen.update((v) => !v);
  }
  closeMobile(): void {
    this.mobileOpen.set(false);
  }

  private restore(): boolean {
    try {
      return localStorage.getItem(KEY) === '1';
    } catch {
      return false;
    }
  }
}

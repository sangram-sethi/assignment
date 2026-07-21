import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { ToastService, ToastKind } from '../../core/services/toast.service';
import { Icon } from './icon';

const ICON_FOR: Record<ToastKind, string> = {
  success: 'checkCircle',
  error: 'xCircle',
  info: 'info',
  warning: 'alert',
};

@Component({
  selector: 'app-toasts',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [Icon],
  template: `
    <div class="stack" aria-live="polite" aria-atomic="true">
      @for (t of toasts.toasts(); track t.id) {
        <div class="toast toast-{{ t.kind }}" animate.enter="t-in" animate.leave="t-out" role="status">
          <span class="ic"><app-icon [name]="iconFor(t.kind)" [size]="18" [strokeWidth]="2.2" /></span>
          <div class="body">
            <p class="title">{{ t.title }}</p>
            @if (t.message) {<p class="msg">{{ t.message }}</p>}
          </div>
          <button class="close" type="button" (click)="toasts.dismiss(t.id)" aria-label="Dismiss">
            <app-icon name="x" [size]="15" />
          </button>
          <span class="rail"></span>
        </div>
      }
    </div>
  `,
  styles: [
    `
      .stack {
        position: fixed;
        top: 1.1rem;
        right: 1.1rem;
        z-index: 2000;
        display: flex;
        flex-direction: column;
        gap: 0.7rem;
        width: min(92vw, 380px);
        pointer-events: none;
      }
      .toast {
        pointer-events: auto;
        position: relative;
        display: grid;
        grid-template-columns: auto 1fr auto;
        align-items: start;
        gap: 0.75rem;
        padding: 0.9rem 0.95rem;
        border-radius: 1.1rem;
        overflow: hidden;
        background: var(--surface-2);
        border: 1px solid var(--line-strong);
        backdrop-filter: blur(24px) saturate(160%);
        box-shadow: var(--shadow-2);
      }
      .ic {
        display: grid;
        place-items: center;
        width: 34px;
        height: 34px;
        border-radius: 0.7rem;
      }
      .toast-success .ic { color: #34d399; background: rgba(52, 211, 153, 0.14); }
      .toast-error .ic { color: #fb7185; background: rgba(251, 113, 133, 0.14); }
      .toast-info .ic { color: #38bdf8; background: rgba(56, 189, 248, 0.14); }
      .toast-warning .ic { color: #fbbf24; background: rgba(251, 191, 36, 0.14); }
      .title { margin: 0; font-weight: 700; font-size: 0.92rem; color: var(--ink); }
      .msg { margin: 0.15rem 0 0; font-size: 0.82rem; color: var(--muted); line-height: 1.35; }
      .close {
        background: transparent;
        border: none;
        color: var(--faint);
        cursor: pointer;
        padding: 0.15rem;
        border-radius: 0.5rem;
        transition: color 0.2s ease, background 0.2s ease;
      }
      .close:hover { color: var(--ink); background: var(--surface); }
      .rail {
        position: absolute;
        left: 0;
        top: 0;
        bottom: 0;
        width: 3px;
      }
      .toast-success .rail { background: linear-gradient(#34d399, #10b981); }
      .toast-error .rail { background: linear-gradient(#fb7185, #ef4444); }
      .toast-info .rail { background: linear-gradient(#38bdf8, #22d3ee); }
      .toast-warning .rail { background: linear-gradient(#fbbf24, #f59e0b); }

      .t-in { animation: toast-in 0.45s cubic-bezier(0.16, 1, 0.3, 1); }
      .t-out { animation: toast-out 0.3s ease forwards; }
      @keyframes toast-in {
        from { opacity: 0; transform: translateX(40px) scale(0.96); }
        to { opacity: 1; transform: translateX(0) scale(1); }
      }
      @keyframes toast-out {
        to { opacity: 0; transform: translateX(40px) scale(0.96); }
      }
    `,
  ],
})
export class Toasts {
  readonly toasts = inject(ToastService);
  iconFor(kind: ToastKind): string {
    return ICON_FOR[kind];
  }
}

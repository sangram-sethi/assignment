import {
  ChangeDetectionStrategy,
  Component,
  input,
  output,
} from '@angular/core';
import { Icon } from './icon';

@Component({
  selector: 'app-modal',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [Icon],
  host: { '(document:keydown.escape)': 'onEsc()' },
  template: `
    @if (open()) {
      <div class="overlay" (click)="onBackdrop($event)">
        <div class="dialog card" animate.enter="pop" role="dialog" aria-modal="true">
          <header class="mh">
            <div class="titles">
              @if (eyebrow()) { <span class="eyebrow">{{ eyebrow() }}</span> }
              <h3 class="font-display">{{ title() }}</h3>
            </div>
            <button class="x" type="button" (click)="close.emit()" aria-label="Close">
              <app-icon name="x" [size]="18" />
            </button>
          </header>
          <div class="mb">
            <ng-content />
          </div>
        </div>
      </div>
    }
  `,
  styles: [
    `
      .overlay {
        position: fixed;
        inset: 0;
        z-index: 1200;
        display: grid;
        place-items: center;
        padding: 1.2rem;
        background: rgba(4, 6, 18, 0.6);
        backdrop-filter: blur(7px);
        animation: fade 0.2s ease;
      }
      @keyframes fade {
        from { opacity: 0; }
        to { opacity: 1; }
      }
      .dialog {
        width: min(94vw, 480px);
        max-height: 90vh;
        overflow: auto;
        padding: 1.4rem 1.5rem 1.5rem;
      }
      .pop {
        animation: mpop 0.34s cubic-bezier(0.16, 1, 0.3, 1);
      }
      @keyframes mpop {
        from { opacity: 0; transform: translateY(14px) scale(0.97); }
        to { opacity: 1; transform: none; }
      }
      .mh {
        display: flex;
        align-items: flex-start;
        justify-content: space-between;
        gap: 1rem;
        margin-bottom: 1.1rem;
      }
      .eyebrow {
        font-size: 0.7rem;
        font-weight: 700;
        text-transform: uppercase;
        letter-spacing: 0.14em;
        color: var(--violet);
      }
      h3 {
        margin: 0.25rem 0 0;
        font-size: 1.3rem;
        font-weight: 800;
        color: var(--ink);
      }
      .x {
        flex: none;
        display: grid;
        place-items: center;
        width: 36px;
        height: 36px;
        border-radius: 0.7rem;
        background: var(--surface);
        border: 1px solid var(--line);
        color: var(--muted);
        cursor: pointer;
        transition: all 0.2s ease;
      }
      .x:hover {
        color: var(--ink);
        border-color: var(--line-strong);
        transform: rotate(90deg);
      }
    `,
  ],
})
export class Modal {
  readonly open = input.required<boolean>();
  readonly title = input.required<string>();
  readonly eyebrow = input<string>('');
  readonly close = output<void>();

  onEsc(): void {
    if (this.open()) this.close.emit();
  }
  onBackdrop(e: MouseEvent): void {
    if ((e.target as HTMLElement).classList.contains('overlay')) this.close.emit();
  }
}

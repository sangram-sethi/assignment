import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { Icon } from './icon';

@Component({
  selector: 'app-empty-state',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [Icon],
  template: `
    <div class="empty anim-pop">
      <span class="ic"><app-icon [name]="icon()" [size]="30" [strokeWidth]="1.6" /></span>
      <h3>{{ title() }}</h3>
      @if (sub()) { <p>{{ sub() }}</p> }
      <div class="cta"><ng-content /></div>
    </div>
  `,
  styles: [
    `
      .empty {
        display: flex;
        flex-direction: column;
        align-items: center;
        text-align: center;
        padding: 3rem 1.5rem;
      }
      .ic {
        display: grid;
        place-items: center;
        width: 74px;
        height: 74px;
        border-radius: 1.3rem;
        color: var(--violet);
        background: var(--surface);
        border: 1px solid var(--line);
        margin-bottom: 1.1rem;
        box-shadow: inset 0 0 0 6px var(--ring-soft);
      }
      h3 {
        margin: 0;
        font-size: 1.15rem;
        font-weight: 700;
        color: var(--ink);
      }
      p {
        margin: 0.5rem 0 0;
        color: var(--muted);
        font-size: 0.9rem;
        max-width: 40ch;
      }
      .cta {
        margin-top: 1.3rem;
      }
    `,
  ],
})
export class EmptyState {
  readonly icon = input<string>('layers');
  readonly title = input.required<string>();
  readonly sub = input<string>('');
}

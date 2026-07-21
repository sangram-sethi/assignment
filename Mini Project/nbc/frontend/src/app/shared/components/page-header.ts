import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { Reveal } from '../directives';

@Component({
  selector: 'app-page-header',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [Reveal],
  template: `
    <header class="ph" reveal="up">
      <div class="lead">
        @if (eyebrow()) { <span class="eyebrow">{{ eyebrow() }}</span> }
        <h1 class="font-display">{{ title() }}</h1>
        @if (subtitle()) { <p class="sub">{{ subtitle() }}</p> }
      </div>
      <div class="actions"><ng-content /></div>
    </header>
  `,
  styles: [
    `
      .ph {
        display: flex;
        align-items: flex-end;
        justify-content: space-between;
        gap: 1.2rem;
        flex-wrap: wrap;
        margin-bottom: 1.6rem;
      }
      .eyebrow {
        display: inline-block;
        font-size: 0.72rem;
        font-weight: 700;
        text-transform: uppercase;
        letter-spacing: 0.16em;
        color: var(--violet);
        margin-bottom: 0.5rem;
      }
      h1 {
        margin: 0;
        font-size: clamp(1.6rem, 3.2vw, 2.25rem);
        font-weight: 800;
        color: var(--ink);
      }
      .sub {
        margin: 0.5rem 0 0;
        color: var(--muted);
        font-size: 0.98rem;
        max-width: 60ch;
      }
      .actions {
        display: flex;
        align-items: center;
        gap: 0.6rem;
        flex-wrap: wrap;
      }
    `,
  ],
})
export class PageHeader {
  readonly title = input.required<string>();
  readonly subtitle = input<string>('');
  readonly eyebrow = input<string>('');
}

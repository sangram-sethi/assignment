import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { Icon } from './icon';
import { CountUp, Tilt } from '../directives';

type CountFormat = 'inr' | 'inr2' | 'compact' | 'number' | 'plain';

@Component({
  selector: 'app-stat-card',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [Icon, CountUp, Tilt],
  template: `
    <div class="stat card card-hover" tilt [tiltMax]="5">
      <div class="top">
        <span class="chip" [style.background]="accent()">
          <app-icon [name]="icon()" [size]="20" />
        </span>
        @if (trend()) {
          <span class="trend" [class.down]="trendDown()">
            <app-icon [name]="trendDown() ? 'arrowRight' : 'trendingUp'" [size]="13" />
            {{ trend() }}
          </span>
        }
      </div>
      <p
        class="val tabular"
        [countUp]="value()"
        [countFormat]="format()"
        [countSuffix]="suffix()"
      >
        0
      </p>
      <p class="lbl">{{ label() }}</p>
      <span class="glow" [style.background]="accent()"></span>
    </div>
  `,
  styles: [
    `
      .stat {
        position: relative;
        overflow: hidden;
        padding: 1.3rem 1.4rem;
      }
      .top {
        display: flex;
        align-items: center;
        justify-content: space-between;
        margin-bottom: 1.1rem;
      }
      .chip {
        display: grid;
        place-items: center;
        width: 46px;
        height: 46px;
        border-radius: 0.9rem;
        color: #fff;
        box-shadow: 0 10px 24px -10px rgba(0, 0, 0, 0.5);
      }
      .trend {
        display: inline-flex;
        align-items: center;
        gap: 0.25rem;
        font-size: 0.74rem;
        font-weight: 700;
        color: #34d399;
        background: rgba(52, 211, 153, 0.12);
        padding: 0.28rem 0.55rem;
        border-radius: 999px;
      }
      .trend.down {
        color: var(--muted);
        background: var(--surface-2);
      }
      .val {
        margin: 0;
        font-family: var(--font-display);
        font-weight: 800;
        font-size: 1.85rem;
        letter-spacing: -0.02em;
        color: var(--ink);
        line-height: 1;
      }
      .lbl {
        margin: 0.45rem 0 0;
        font-size: 0.85rem;
        color: var(--muted);
        font-weight: 500;
      }
      .glow {
        position: absolute;
        right: -40px;
        bottom: -50px;
        width: 130px;
        height: 130px;
        border-radius: 50%;
        opacity: 0.14;
        filter: blur(26px);
      }
    `,
  ],
})
export class StatCard {
  readonly label = input.required<string>();
  readonly value = input.required<number>();
  readonly icon = input<string>('spark');
  readonly format = input<CountFormat>('number');
  readonly suffix = input<string>('');
  readonly accent = input<string>('linear-gradient(135deg,#7c5cff,#b06bff)');
  readonly trend = input<string>('');
  readonly trendDown = input<boolean>(false);
}

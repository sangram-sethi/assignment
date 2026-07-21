import {
  ChangeDetectionStrategy,
  Component,
  ElementRef,
  OnDestroy,
  afterNextRender,
  effect,
  inject,
  input,
  viewChild,
} from '@angular/core';
import Chart from 'chart.js/auto';
import { formatINR } from '../format';
import { ThemeService } from '../../core/services/theme.service';

export interface DonutSegment {
  label: string;
  value: number;
  color: string;
}

@Component({
  selector: 'app-donut-chart',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="wrap" [style.height.px]="height()">
      <canvas #cv></canvas>
      @if (centerTop() || centerBottom()) {
        <div class="center">
          <span class="top tabular">{{ centerTop() }}</span>
          <span class="bottom">{{ centerBottom() }}</span>
        </div>
      }
    </div>
  `,
  styles: [
    `
      .wrap {
        position: relative;
      }
      canvas {
        position: relative;
        z-index: 1;
      }
      .center {
        position: absolute;
        inset: 0;
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        pointer-events: none;
      }
      .top {
        font-family: var(--font-display);
        font-weight: 800;
        font-size: 1.4rem;
        color: var(--ink);
      }
      .bottom {
        font-size: 0.74rem;
        color: var(--faint);
        margin-top: 3px;
        letter-spacing: 0.04em;
      }
    `,
  ],
})
export class DonutChart implements OnDestroy {
  private readonly theme = inject(ThemeService);
  readonly segments = input.required<DonutSegment[]>();
  readonly height = input<number>(220);
  readonly centerTop = input<string>('');
  readonly centerBottom = input<string>('');
  readonly cutout = input<string>('72%');
  readonly currency = input<boolean>(true);

  private readonly canvasRef = viewChild<ElementRef<HTMLCanvasElement>>('cv');
  private chart?: Chart;

  constructor() {
    afterNextRender(() => this.build());
    effect(() => {
      const s = this.segments();
      this.theme.theme();
      if (this.chart) this.update(s);
    });
  }

  private build(): void {
    const el = this.canvasRef()?.nativeElement;
    if (!el) return;
    this.chart = new Chart(el, {
      type: 'doughnut',
      data: this.data(this.segments()),
      options: {
        responsive: true,
        maintainAspectRatio: false,
        cutout: this.cutout(),
        plugins: {
          legend: { display: false },
          tooltip: {
            padding: 10,
            backgroundColor: 'rgba(10,14,32,0.92)',
            titleColor: '#fff',
            bodyColor: '#cbd0e6',
            borderColor: 'rgba(255,255,255,0.12)',
            borderWidth: 1,
            callbacks: {
              label: (ctx) =>
                ` ${ctx.label}: ${this.currency() ? formatINR(ctx.parsed) : ctx.parsed}`,
            },
          },
        },
        animation: { animateRotate: true, duration: 900, easing: 'easeOutQuart' },
      },
    });
  }

  private data(s: DonutSegment[]) {
    return {
      labels: s.map((x) => x.label),
      datasets: [
        {
          data: s.map((x) => x.value),
          backgroundColor: s.map((x) => x.color),
          borderColor: 'transparent',
          borderWidth: 0,
          hoverOffset: 8,
          spacing: 2,
          borderRadius: 6,
        },
      ],
    };
  }

  private update(s: DonutSegment[]): void {
    if (!this.chart) return;
    this.chart.data = this.data(s);
    this.chart.update();
  }

  ngOnDestroy(): void {
    this.chart?.destroy();
  }
}

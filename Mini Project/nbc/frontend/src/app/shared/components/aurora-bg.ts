import { ChangeDetectionStrategy, Component } from '@angular/core';

/** Full-viewport animated aurora canvas with a subtle grid + grain overlay. */
@Component({
  selector: 'app-aurora-bg',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="aurora" aria-hidden="true">
      <span class="blob blob-a"></span>
      <span class="blob blob-b"></span>
      <span class="blob blob-c"></span>
      <div class="grid"></div>
      <div class="grain"></div>
    </div>
  `,
  styles: [
    `
      .aurora {
        position: fixed;
        inset: 0;
        z-index: 0;
        overflow: hidden;
        pointer-events: none;
      }
      .blob {
        position: absolute;
        border-radius: 50%;
        filter: blur(70px);
        opacity: 0.55;
        mix-blend-mode: screen;
        will-change: transform;
      }
      .blob-a {
        width: 46vw;
        height: 46vw;
        left: -8vw;
        top: -10vh;
        background: radial-gradient(circle at 30% 30%, #7c5cff, transparent 62%);
        animation: aurora-drift 22s ease-in-out infinite;
      }
      .blob-b {
        width: 40vw;
        height: 40vw;
        right: -6vw;
        top: -6vh;
        background: radial-gradient(circle at 60% 40%, #38bdf8, transparent 62%);
        animation: aurora-drift 26s ease-in-out infinite reverse;
      }
      .blob-c {
        width: 44vw;
        height: 44vw;
        left: 30vw;
        bottom: -22vh;
        background: radial-gradient(circle at 50% 50%, #ff6ea9, transparent 60%);
        animation: aurora-drift 30s ease-in-out infinite;
      }
      [data-theme='light'] .blob {
        opacity: 0.3;
      }
      .grid {
        position: absolute;
        inset: 0;
        background-image: linear-gradient(var(--line) 1px, transparent 1px),
          linear-gradient(90deg, var(--line) 1px, transparent 1px);
        background-size: 56px 56px;
        -webkit-mask-image: radial-gradient(ellipse 80% 60% at 50% 0%, #000 30%, transparent 75%);
        mask-image: radial-gradient(ellipse 80% 60% at 50% 0%, #000 30%, transparent 75%);
        opacity: 0.35;
      }
      .grain {
        position: absolute;
        inset: 0;
        opacity: 0.035;
        background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='140' height='140'%3E%3Cfilter id='n'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.85' numOctaves='3'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23n)'/%3E%3C/svg%3E");
      }
    `,
  ],
})
export class AuroraBackground {}

import { ChangeDetectionStrategy, Component, input } from '@angular/core';

/** Aurora brand mark + optional wordmark. */
@Component({
  selector: 'app-logo',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <span class="logo">
      <span class="mark" [style.width.px]="size()" [style.height.px]="size()">
        <svg viewBox="0 0 64 64" [attr.width]="size()" [attr.height]="size()" fill="none">
          <defs>
            <linearGradient [attr.id]="gid" x1="0" y1="0" x2="1" y2="1">
              <stop offset="0" stop-color="#7c5cff" />
              <stop offset="0.5" stop-color="#b06bff" />
              <stop offset="1" stop-color="#ff6ea9" />
            </linearGradient>
            <linearGradient [attr.id]="tid" x1="0" y1="0" x2="1" y2="1">
              <stop offset="0" stop-color="#22d3ee" />
              <stop offset="1" stop-color="#34d399" />
            </linearGradient>
          </defs>
          <rect x="4" y="4" width="56" height="56" rx="17" [attr.fill]="'url(#' + gid + ')'" opacity="0.16" />
          <rect x="4" y="4" width="56" height="56" rx="17" [attr.stroke]="'url(#' + gid + ')'" stroke-width="1.4" opacity="0.5" />
          <path
            d="M22 44 L32 18 L42 44"
            fill="none"
            [attr.stroke]="'url(#' + gid + ')'"
            stroke-width="4.6"
            stroke-linecap="round"
            stroke-linejoin="round"
          />
          <path d="M26.5 36 H37.5" [attr.stroke]="'url(#' + tid + ')'" stroke-width="4.6" stroke-linecap="round" />
          <circle cx="32" cy="13.5" r="2.6" fill="#38bdf8" />
        </svg>
      </span>
      @if (showText()) {
        <span class="word font-display">
          Aurora<span class="sub">Loan Studio</span>
        </span>
      }
    </span>
  `,
  styles: [
    `
      .logo {
        display: inline-flex;
        align-items: center;
        gap: 0.7rem;
      }
      .mark {
        display: inline-flex;
        filter: drop-shadow(0 6px 20px rgba(124, 92, 255, 0.35));
      }
      .word {
        display: flex;
        flex-direction: column;
        line-height: 1;
        font-weight: 800;
        font-size: 1.05rem;
        letter-spacing: -0.02em;
        color: var(--ink);
      }
      .sub {
        font-family: var(--font-sans);
        font-weight: 600;
        font-size: 0.62rem;
        letter-spacing: 0.22em;
        text-transform: uppercase;
        color: var(--faint);
        margin-top: 3px;
      }
    `,
  ],
})
export class Logo {
  readonly size = input<number>(38);
  readonly showText = input<boolean>(true);
  private static seq = 0;
  readonly gid = `lg${Logo.seq++}`;
  readonly tid = `lt${Logo.seq++}`;
}

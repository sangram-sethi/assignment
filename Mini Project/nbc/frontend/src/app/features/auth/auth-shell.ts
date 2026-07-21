import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { Logo } from '../../shared/components/logo';
import { Icon } from '../../shared/components/icon';
import { Reveal } from '../../shared/directives';

interface Feature {
  icon: string;
  title: string;
  sub: string;
}

/** Split-screen brand showcase shared by the login and register screens. */
@Component({
  selector: 'app-auth-shell',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [Logo, Icon, Reveal],
  template: `
    <div class="auth">
      <section class="showcase">
        <div class="sc-inner">
          <a class="brand" href="/"><app-logo [size]="42" /></a>

          <div class="hero">
            <span class="eyebrow" reveal="up">
              <span class="ping"></span> Next-gen lending studio
            </span>
            <h1 reveal="up" [revealDelay]="0.05">
              Lending,<br /><span class="text-gradient">reimagined.</span>
            </h1>
            <p class="lede" reveal="up" [revealDelay]="0.12">
              Apply, quote EMIs and track approvals in a beautifully simple studio — crafted for
              clarity and speed.
            </p>
          </div>

          <ul class="feats">
            @for (f of features; track f.title; let i = $index) {
              <li reveal="up" [revealDelay]="0.18 + i * 0.08">
                <span class="fi"><app-icon [name]="f.icon" [size]="18" /></span>
                <span>
                  <strong>{{ f.title }}</strong>
                  <em>{{ f.sub }}</em>
                </span>
              </li>
            }
          </ul>

          <div class="floaty glass anim-float" reveal="scale" [revealDelay]="0.3">
            <div class="fl-row">
              <span class="fl-ic"><app-icon name="trendingUp" [size]="18" /></span>
              <div>
                <p class="fl-k">₹5,00,000 approved</p>
                <p class="fl-s">Home Loan · 8.5% · 60 months</p>
              </div>
            </div>
            <div class="spark"></div>
          </div>
        </div>
      </section>

      <section class="form-side">
        <div class="form-wrap">
          <div class="mobile-brand"><app-logo [size]="40" /></div>
          <ng-content />
        </div>
      </section>
    </div>
  `,
  styleUrl: './auth-shell.scss',
})
export class AuthShell {
  readonly mode = input<'login' | 'register'>('login');
  readonly features: Feature[] = [
    { icon: 'zap', title: 'Instant EMI quotes', sub: 'Know your monthly outgo before you apply.' },
    { icon: 'shield', title: 'Bank-grade security', sub: 'JWT-secured sessions, always encrypted.' },
    { icon: 'sparkles', title: 'Real-time approvals', sub: 'Track every decision as it happens.' },
  ];
}

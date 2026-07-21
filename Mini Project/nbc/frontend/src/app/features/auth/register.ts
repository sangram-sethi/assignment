import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { toSignal } from '@angular/core/rxjs-interop';
import { Router, RouterLink } from '@angular/router';
import { AuthShell } from './auth-shell';
import { Icon } from '../../shared/components/icon';
import { Reveal } from '../../shared/directives';
import { AuthService } from '../../core/services/auth.service';
import { ToastService } from '../../core/services/toast.service';
import { apiErrorMessage } from '../../shared/http-error';

@Component({
  selector: 'app-register',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [AuthShell, Icon, Reveal, ReactiveFormsModule, RouterLink],
  template: `
    <app-auth-shell mode="register">
      <div class="card auth-card" reveal="up">
        <div class="pad">
          <h2 class="ttl font-display">Create your account</h2>
          <p class="sub">Join Aurora and unlock a smarter way to borrow.</p>

          @if (error()) {
            <div class="form-alert" animate.enter="anim-pop">
              <app-icon name="alert" [size]="16" /> {{ error() }}
            </div>
          }

          <form [formGroup]="form" (ngSubmit)="submit()" class="frm">
            <div class="field">
              <label class="label" for="n">Full name</label>
              <div class="control">
                <app-icon class="lead" name="user" [size]="18" />
                <input id="n" class="input has-lead" formControlName="name" placeholder="Jane Doe" />
              </div>
              @if (invalid('name')) { <span class="field-err"><app-icon name="alert" [size]="13" /> Name is required</span> }
            </div>

            <div class="grid2">
              <div class="field">
                <label class="label" for="e">Email</label>
                <div class="control">
                  <app-icon class="lead" name="mail" [size]="18" />
                  <input id="e" class="input has-lead" type="email" formControlName="email" placeholder="jane@email.com" />
                </div>
                @if (invalid('email')) { <span class="field-err"><app-icon name="alert" [size]="13" /> Enter a valid email</span> }
              </div>
              <div class="field">
                <label class="label" for="ph">Phone</label>
                <div class="control">
                  <app-icon class="lead" name="phone" [size]="18" />
                  <input id="ph" class="input has-lead" formControlName="phone" placeholder="9876543210" inputmode="numeric" />
                </div>
                @if (invalid('phone')) { <span class="field-err"><app-icon name="alert" [size]="13" /> 7–15 digits</span> }
              </div>
            </div>

            <div class="field">
              <label class="label" for="inc">Annual income (₹)</label>
              <div class="control">
                <app-icon class="lead" name="wallet" [size]="18" />
                <input id="inc" class="input has-lead" type="number" formControlName="annualIncome" placeholder="850000" min="1" />
              </div>
              @if (invalid('annualIncome')) { <span class="field-err"><app-icon name="alert" [size]="13" /> Enter your annual income</span> }
            </div>

            <div class="grid2">
              <div class="field">
                <label class="label" for="un">Username</label>
                <div class="control">
                  <app-icon class="lead" name="sparkles" [size]="18" />
                  <input id="un" class="input has-lead" formControlName="username" autocomplete="username" placeholder="jane.doe" />
                </div>
                @if (invalid('username')) { <span class="field-err"><app-icon name="alert" [size]="13" /> Username is required</span> }
              </div>
              <div class="field">
                <label class="label" for="pw">Password</label>
                <div class="control">
                  <app-icon class="lead" name="lock" [size]="18" />
                  <input
                    id="pw"
                    class="input has-lead has-trail"
                    [type]="showPw() ? 'text' : 'password'"
                    formControlName="password"
                    autocomplete="new-password"
                    placeholder="min. 6 characters"
                  />
                  <button class="trail" type="button" (click)="showPw.set(!showPw())" aria-label="Toggle password">
                    <app-icon [name]="showPw() ? 'eyeOff' : 'eye'" [size]="17" />
                  </button>
                </div>
                @if (invalid('password')) { <span class="field-err"><app-icon name="alert" [size]="13" /> At least 6 characters</span> }
              </div>
            </div>

            @if ((password() ?? '').length > 0) {
              <div class="strength">
                <div class="bars">
                  @for (i of [0, 1, 2, 3]; track i) {
                    <span class="bar" [class.on]="strength().score > i" [style.background]="strength().score > i ? strength().color : ''"></span>
                  }
                </div>
                <span class="s-label" [style.color]="strength().color">{{ strength().label }}</span>
              </div>
            }

            <button class="btn btn-primary w-full submit" type="submit" [disabled]="loading()">
              @if (loading()) { <span class="spinner"></span> Creating account… }
              @else { Create account <app-icon name="arrowRight" [size]="18" /> }
            </button>
          </form>

          <p class="alt">Already have an account? <a routerLink="/login">Sign in</a></p>
        </div>
      </div>
    </app-auth-shell>
  `,
  styles: [
    `
      .pad { padding: clamp(1.5rem, 3.5vw, 2.2rem); }
      .ttl { margin: 0; font-size: 1.75rem; font-weight: 800; color: var(--ink); }
      .sub { margin: 0.4rem 0 1.4rem; color: var(--muted); font-size: 0.94rem; }
      .form-alert { margin-bottom: 1.1rem; }
      .frm { display: flex; flex-direction: column; gap: 1rem; }
      .grid2 { display: grid; grid-template-columns: 1fr 1fr; gap: 1rem; }
      .submit { margin-top: 0.5rem; height: 50px; font-size: 0.98rem; }
      .alt { margin: 1.3rem 0 0; text-align: center; color: var(--muted); font-size: 0.9rem; }
      .alt a { color: var(--violet); font-weight: 700; text-decoration: none; }
      .alt a:hover { text-decoration: underline; }
      .strength { display: flex; align-items: center; gap: 0.7rem; }
      .bars { display: flex; gap: 5px; flex: 1; }
      .bar {
        flex: 1;
        height: 5px;
        border-radius: 999px;
        background: var(--line-strong);
        transition: background 0.3s ease;
      }
      .s-label { font-size: 0.74rem; font-weight: 700; min-width: 56px; text-align: right; }
      @media (max-width: 480px) {
        .grid2 { grid-template-columns: 1fr; }
      }
    `,
  ],
})
export class Register {
  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly toast = inject(ToastService);

  readonly loading = signal(false);
  readonly error = signal<string | null>(null);
  readonly showPw = signal(false);

  readonly form = this.fb.group({
    name: ['', Validators.required],
    email: ['', [Validators.required, Validators.email]],
    phone: ['', [Validators.required, Validators.pattern(/^\d{7,15}$/)]],
    annualIncome: [null as number | null, [Validators.required, Validators.min(1)]],
    username: ['', Validators.required],
    password: ['', [Validators.required, Validators.minLength(6)]],
  });

  readonly password = toSignal(this.form.controls.password.valueChanges, { initialValue: '' });

  readonly strength = computed(() => {
    const pw = this.password() ?? '';
    let score = 0;
    if (pw.length >= 6) score++;
    if (pw.length >= 10) score++;
    if (/[A-Z]/.test(pw) && /[a-z]/.test(pw)) score++;
    if (/\d/.test(pw) && /[^A-Za-z0-9]/.test(pw)) score++;
    const map = [
      { label: 'Weak', color: '#fb7185' },
      { label: 'Fair', color: '#fbbf24' },
      { label: 'Good', color: '#38bdf8' },
      { label: 'Strong', color: '#34d399' },
    ];
    const idx = Math.max(0, Math.min(score, 4) - 1);
    return { score, ...map[idx] };
  });

  invalid(name: keyof typeof this.form.controls): boolean {
    const c = this.form.controls[name];
    return c.invalid && (c.touched || c.dirty);
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.loading.set(true);
    this.error.set(null);
    const v = this.form.getRawValue();
    const payload = {
      name: v.name!,
      email: v.email!,
      phone: v.phone!,
      annualIncome: Number(v.annualIncome),
      username: v.username!,
      password: v.password!,
    };
    this.auth.register(payload).subscribe({
      next: () => {
        // Registration only creates the customer; sign in to obtain a token.
        this.auth.login({ username: payload.username, password: payload.password }).subscribe({
          next: (res) => {
            this.loading.set(false);
            this.toast.success('Account created', `Welcome to Aurora, ${res.username}!`);
            void this.router.navigateByUrl(this.auth.homeRoute());
          },
          error: () => {
            this.loading.set(false);
            this.toast.success('Account created', 'Please sign in to continue.');
            void this.router.navigateByUrl('/login');
          },
        });
      },
      error: (err) => {
        this.loading.set(false);
        this.error.set(apiErrorMessage(err, 'Could not create your account.'));
      },
    });
  }
}

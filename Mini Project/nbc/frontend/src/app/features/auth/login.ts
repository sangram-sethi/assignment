import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AuthShell } from './auth-shell';
import { Icon } from '../../shared/components/icon';
import { Reveal } from '../../shared/directives';
import { AuthService } from '../../core/services/auth.service';
import { ToastService } from '../../core/services/toast.service';
import { apiErrorMessage } from '../../shared/http-error';

@Component({
  selector: 'app-login',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [AuthShell, Icon, Reveal, ReactiveFormsModule, RouterLink],
  template: `
    <app-auth-shell mode="login">
      <div class="card auth-card" reveal="up">
        <div class="pad">
          <h2 class="ttl font-display">Welcome back</h2>
          <p class="sub">Sign in to continue to your Aurora studio.</p>

          @if (error()) {
            <div class="form-alert" animate.enter="anim-pop">
              <app-icon name="alert" [size]="16" /> {{ error() }}
            </div>
          }

          <form [formGroup]="form" (ngSubmit)="submit()" class="frm">
            <div class="field">
              <label class="label" for="u">Username</label>
              <div class="control">
                <app-icon class="lead" name="user" [size]="18" />
                <input
                  id="u"
                  class="input has-lead"
                  formControlName="username"
                  autocomplete="username"
                  placeholder="your.username"
                />
              </div>
              @if (invalid('username')) {
                <span class="field-err"><app-icon name="alert" [size]="13" /> Username is required</span>
              }
            </div>

            <div class="field">
              <label class="label" for="p">Password</label>
              <div class="control">
                <app-icon class="lead" name="lock" [size]="18" />
                <input
                  id="p"
                  class="input has-lead has-trail"
                  [type]="showPw() ? 'text' : 'password'"
                  formControlName="password"
                  autocomplete="current-password"
                  placeholder="••••••••"
                />
                <button
                  class="trail"
                  type="button"
                  (click)="showPw.set(!showPw())"
                  [attr.aria-label]="showPw() ? 'Hide password' : 'Show password'"
                >
                  <app-icon [name]="showPw() ? 'eyeOff' : 'eye'" [size]="17" />
                </button>
              </div>
              @if (invalid('password')) {
                <span class="field-err"><app-icon name="alert" [size]="13" /> Password is required</span>
              }
            </div>

            <button class="btn btn-primary w-full submit" type="submit" [disabled]="loading()">
              @if (loading()) {
                <span class="spinner"></span> Signing in…
              } @else {
                Sign in <app-icon name="arrowRight" [size]="18" />
              }
            </button>
          </form>

          <p class="alt">
            New to Aurora? <a routerLink="/register">Create an account</a>
          </p>
        </div>
      </div>
    </app-auth-shell>
  `,
  styles: [
    `
      .auth-card {
        overflow: hidden;
      }
      .pad {
        padding: clamp(1.6rem, 4vw, 2.4rem);
      }
      .ttl {
        margin: 0;
        font-size: 1.9rem;
        font-weight: 800;
        color: var(--ink);
      }
      .sub {
        margin: 0.4rem 0 1.5rem;
        color: var(--muted);
        font-size: 0.95rem;
      }
      .form-alert {
        margin-bottom: 1.1rem;
      }
      .frm {
        display: flex;
        flex-direction: column;
        gap: 1.1rem;
      }
      .submit {
        margin-top: 0.4rem;
        height: 50px;
        font-size: 0.98rem;
      }
      .alt {
        margin: 1.4rem 0 0;
        text-align: center;
        color: var(--muted);
        font-size: 0.9rem;
      }
      .alt a {
        color: var(--violet);
        font-weight: 700;
        text-decoration: none;
      }
      .alt a:hover {
        text-decoration: underline;
      }
    `,
  ],
})
export class Login {
  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly toast = inject(ToastService);

  readonly loading = signal(false);
  readonly error = signal<string | null>(null);
  readonly showPw = signal(false);

  readonly form = this.fb.nonNullable.group({
    username: ['', Validators.required],
    password: ['', Validators.required],
  });

  invalid(name: 'username' | 'password'): boolean {
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
    this.auth.login(this.form.getRawValue()).subscribe({
      next: (res) => {
        this.loading.set(false);
        this.toast.success('Welcome back', `Signed in as ${res.username}`);
        const returnUrl = this.route.snapshot.queryParamMap.get('returnUrl');
        void this.router.navigateByUrl(returnUrl || this.auth.homeRoute());
      },
      error: (err) => {
        this.loading.set(false);
        this.error.set(apiErrorMessage(err, 'Invalid username or password.'));
      },
    });
  }
}

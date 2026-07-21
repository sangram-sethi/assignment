import { CommonModule } from "@angular/common";
import { Component, inject, signal } from "@angular/core";
import { FormBuilder, ReactiveFormsModule, Validators } from "@angular/forms";
import { HttpErrorResponse } from "@angular/common/http";
import { AuthService } from "../auth.service";
import { Router } from "@angular/router";
import { toUserMessage } from "../../http/http-error.util";

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './login.component.html',
})

export class LoginComponent {
    private readonly fb = inject(FormBuilder);
    private readonly auth = inject(AuthService);
    private readonly router = inject(Router);

    readonly loading = signal(false);
    readonly error = signal<string | null>(null);

    readonly form = this.fb.nonNullable.group({
        username: ['', [Validators.required]],
        password: ['', [Validators.required, Validators.minLength(7)]],
    });

    submit(): void {
        if (this.form.invalid) {
            this.form.markAllAsTouched();
            return;
        }

        this.loading.set(true);
        this.error.set(null);

        const {username, password} = this.form.getRawValue();

        // The login endpoint itself validates the credentials and returns a JWT.
        this.auth.login(username, password).subscribe({
            next: () => {
                this.loading.set(false);
                this.router.navigate(['/products']);
            },
            error: (err: HttpErrorResponse) => {
                this.loading.set(false);
                // 401 here specifically means bad credentials; everything else
                // (notably status 0 = backend unreachable) gets a clearer message.
                this.error.set(
                    err.status === 401
                        ? 'Invalid username or password.'
                        : toUserMessage(err)
                );
            }
        });
    }
}
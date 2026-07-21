import { Injectable, computed, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { API_BASE } from '../api.config';
import {
  AuthUser,
  CustomerResponse,
  JwtResponse,
  LoginRequest,
  RegisterCustomerRequest,
  Role,
} from '../models';

const STORAGE_KEY = 'aurora-auth';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly api = inject(API_BASE);
  private readonly router = inject(Router);

  private readonly _user = signal<AuthUser | null>(this.restore());

  readonly user = this._user.asReadonly();
  readonly isAuthenticated = computed(() => this._user() !== null);
  readonly role = computed<Role | null>(() => this._user()?.role ?? null);
  readonly isCustomer = computed(() => this.role() === 'CUSTOMER');
  readonly isApprover = computed(() => this.role() === 'LOAN_APPROVER');
  readonly username = computed(() => this._user()?.username ?? '');

  token(): string | null {
    return this._user()?.token ?? null;
  }

  login(payload: LoginRequest): Observable<JwtResponse> {
    return this.http.post<JwtResponse>(`${this.api}/auth/login`, payload).pipe(
      tap((res) => this.persist({ username: res.username, role: res.role, token: res.token })),
    );
  }

  register(payload: RegisterCustomerRequest): Observable<CustomerResponse> {
    return this.http.post<CustomerResponse>(`${this.api}/auth/register`, payload);
  }

  logout(redirect = true): void {
    this._user.set(null);
    try {
      localStorage.removeItem(STORAGE_KEY);
    } catch {
      /* ignore */
    }
    if (redirect) {
      void this.router.navigate(['/login']);
    }
  }

  /** Home route for the current principal. */
  homeRoute(): string {
    return this.isApprover() ? '/approvals' : '/dashboard';
  }

  private persist(user: AuthUser): void {
    this._user.set(user);
    try {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(user));
    } catch {
      /* ignore */
    }
  }

  private restore(): AuthUser | null {
    try {
      const raw = localStorage.getItem(STORAGE_KEY);
      if (!raw) return null;
      const parsed = JSON.parse(raw) as AuthUser;
      if (parsed && parsed.token && parsed.role) return parsed;
    } catch {
      /* ignore */
    }
    return null;
  }
}

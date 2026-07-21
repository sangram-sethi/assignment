import { computed, inject, Injectable, signal } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import { firstValueFrom, Observable, tap } from "rxjs";
import { environment } from "../../../environments/environment";
import { CurrentUser, LoginRequest, LoginResponse } from "./auth.model";

@Injectable({
  providedIn: 'root'
})
export class AuthService {
    private readonly http = inject(HttpClient);
    private readonly baseUrl = `${environment.apiUrl}/auth`;

    // We track WHO is logged in, not the token. The JWT lives in an httpOnly
    // cookie set by the backend, so JavaScript can neither read nor store it —
    // this removes both the XSS token-theft risk and the reload-logout problem
    // (the cookie survives reloads; hydrate() restores identity from the server).
    private readonly currentUser = signal<CurrentUser | null>(null);

    readonly isLoggedIn = computed(() => this.currentUser() !== null);
    readonly roles = computed(() => this.currentUser()?.roles ?? []);
    readonly isAdmin = computed(() => this.roles().includes('ROLE_ADMIN'));
    readonly username = computed(() => this.currentUser()?.username ?? null);

    // POST /api/auth/login — backend sets the httpOnly cookie; we keep identity.
    login(username: string, password: string): Observable<LoginResponse> {
        const body: LoginRequest = { username, password };
        return this.http.post<LoginResponse>(`${this.baseUrl}/login`, body).pipe(
            tap((res) => this.currentUser.set({ username: res.username, roles: res.roles }))
        );
    }

    // POST /api/auth/logout — backend clears the cookie; we clear identity.
    logout(): Observable<void> {
        return this.http.post<void>(`${this.baseUrl}/logout`, {}).pipe(
            tap(() => this.clearSession())
        );
    }

    // Clears local identity only (no server call). Used by the error interceptor
    // when a 401 tells us the cookie is already gone/expired.
    clearSession(): void {
        this.currentUser.set(null);
    }

    // Runs on app startup: asks the server who we are so a page reload keeps the
    // session. A 401 simply means "not logged in".
    async hydrate(): Promise<void> {
        try {
            const user = await firstValueFrom(
                this.http.get<CurrentUser>(`${this.baseUrl}/me`)
            );
            this.currentUser.set(user);
        } catch {
            this.currentUser.set(null);
        }
    }
}
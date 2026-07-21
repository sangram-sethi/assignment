import {
  ChangeDetectionStrategy,
  Component,
  ElementRef,
  computed,
  inject,
  signal,
} from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthService } from '../core/services/auth.service';
import { ThemeService } from '../core/services/theme.service';
import { CommandPaletteService } from '../core/services/command-palette.service';
import { LayoutStore } from './layout.store';
import { Icon } from '../shared/components/icon';
import { initials } from '../shared/format';

@Component({
  selector: 'app-topbar',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, Icon],
  host: { '(document:click)': 'onDocClick($event)' },
  template: `
    <header class="topbar">
      <div class="left">
        <button class="hb" type="button" (click)="layout.toggleMobile()" aria-label="Open menu">
          <app-icon name="menu" [size]="20" />
        </button>
        <button class="search" type="button" (click)="cp.show()">
          <app-icon name="search" [size]="17" />
          <span class="ph">Search or jump to…</span>
          <span class="keys"><span class="kbd">⌘</span><span class="kbd">K</span></span>
        </button>
      </div>

      <div class="right">
        <button class="ico theme" type="button" (click)="theme.toggle()" aria-label="Toggle theme">
          <app-icon [name]="theme.theme() === 'dark' ? 'sun' : 'moon'" [size]="19" />
        </button>

        <div class="userwrap">
          <button class="userbtn" type="button" (click)="toggleMenu($event)">
            <span class="avatar">{{ userInitials() }}</span>
            <span class="meta">
              <span class="nm">{{ auth.username() }}</span>
              <span class="rl">{{ roleLabel() }}</span>
            </span>
            <app-icon name="chevronDown" [size]="15" class="chev" [class.up]="menuOpen()" />
          </button>

          @if (menuOpen()) {
            <div class="menu glass-strong" animate.enter="pop" animate.leave="pop-out">
              <div class="mhead">
                <span class="avatar lg">{{ userInitials() }}</span>
                <div>
                  <p class="nm">{{ auth.username() }}</p>
                  <p class="rl">{{ roleLabel() }}</p>
                </div>
              </div>
              <div class="divider"></div>
              <a class="mi" routerLink="/profile" (click)="close()">
                <app-icon name="user" [size]="17" /> Profile
              </a>
              <button class="mi" type="button" (click)="theme.toggle()">
                <app-icon [name]="theme.theme() === 'dark' ? 'sun' : 'moon'" [size]="17" />
                {{ theme.theme() === 'dark' ? 'Light' : 'Dark' }} mode
              </button>
              <div class="divider"></div>
              <button class="mi danger" type="button" (click)="signOut()">
                <app-icon name="logout" [size]="17" /> Sign out
              </button>
            </div>
          }
        </div>
      </div>
    </header>
  `,
  styleUrl: './topbar.scss',
})
export class Topbar {
  readonly auth = inject(AuthService);
  readonly theme = inject(ThemeService);
  readonly cp = inject(CommandPaletteService);
  readonly layout = inject(LayoutStore);
  private readonly el = inject(ElementRef<HTMLElement>);

  readonly menuOpen = signal(false);
  readonly roleLabel = computed(() => (this.auth.isApprover() ? 'Loan Approver' : 'Customer'));
  readonly userInitials = computed(() => initials(this.auth.username()).toUpperCase());

  toggleMenu(e: Event): void {
    e.stopPropagation();
    this.menuOpen.update((v) => !v);
  }
  close(): void {
    this.menuOpen.set(false);
  }
  signOut(): void {
    this.close();
    this.auth.logout();
  }
  onDocClick(e: MouseEvent): void {
    if (this.menuOpen() && !this.el.nativeElement.contains(e.target as Node)) this.close();
  }
}

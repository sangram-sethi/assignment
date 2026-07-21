import { ChangeDetectionStrategy, Component, computed, inject } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../core/services/auth.service';
import { LayoutStore } from './layout.store';
import { Icon } from '../shared/components/icon';
import { Logo } from '../shared/components/logo';
import { initials } from '../shared/format';

interface NavItem {
  label: string;
  icon: string;
  link: string;
}

@Component({
  selector: 'app-sidebar',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, RouterLinkActive, Icon, Logo],
  template: `
    <aside class="sidebar" [class.collapsed]="layout.collapsed()" [class.open]="layout.mobileOpen()">
      <div class="head">
        <a class="brand" routerLink="/" (click)="onNav()" aria-label="Aurora home">
          <app-logo [showText]="!layout.collapsed()" [size]="36" />
        </a>
        <button
          class="collapse"
          type="button"
          (click)="layout.toggleCollapsed()"
          aria-label="Collapse sidebar"
        >
          <app-icon [name]="layout.collapsed() ? 'chevronRight' : 'chevronLeft'" [size]="16" />
        </button>
      </div>

      <nav class="nav no-scrollbar">
        <p class="group">Menu</p>
        @for (item of items(); track item.link) {
          <a
            class="item"
            [routerLink]="item.link"
            routerLinkActive="active"
            (click)="onNav()"
            [attr.data-label]="item.label"
          >
            <span class="ic"><app-icon [name]="item.icon" [size]="19" /></span>
            <span class="txt">{{ item.label }}</span>
            <span class="pip"></span>
          </a>
        }

        <p class="group">Account</p>
        <a
          class="item"
          routerLink="/profile"
          routerLinkActive="active"
          (click)="onNav()"
          data-label="Profile"
        >
          <span class="ic"><app-icon name="user" [size]="19" /></span>
          <span class="txt">Profile</span>
          <span class="pip"></span>
        </a>
      </nav>

      <div class="foot">
        <div class="upsell" [class.hide]="layout.collapsed()">
          <div class="glowdot"></div>
          <p class="up-title">Need a hand?</p>
          <p class="up-sub">Press <span class="kbd">⌘</span><span class="kbd">K</span> anywhere</p>
        </div>
        <div class="user">
          <span class="avatar">{{ userInitials() }}</span>
          <div class="who">
            <span class="name">{{ auth.username() }}</span>
            <span class="role">{{ roleLabel() }}</span>
          </div>
          <button class="out" type="button" (click)="auth.logout()" aria-label="Sign out">
            <app-icon name="logout" [size]="17" />
          </button>
        </div>
      </div>
    </aside>
  `,
  styleUrl: './sidebar.scss',
})
export class Sidebar {
  readonly auth = inject(AuthService);
  readonly layout = inject(LayoutStore);

  readonly items = computed<NavItem[]>(() =>
    this.auth.isApprover()
      ? [{ label: 'Approvals', icon: 'shieldCheck', link: '/approvals' }]
      : [
          { label: 'Dashboard', icon: 'grid', link: '/dashboard' },
          { label: 'Apply', icon: 'plus', link: '/apply' },
          { label: 'EMI Calculator', icon: 'calculator', link: '/calculator' },
          { label: 'My Loans', icon: 'list', link: '/loans' },
        ],
  );

  readonly roleLabel = computed(() => (this.auth.isApprover() ? 'Loan Approver' : 'Customer'));
  readonly userInitials = computed(() => initials(this.auth.username()).toUpperCase());

  onNav(): void {
    this.layout.closeMobile();
  }
}

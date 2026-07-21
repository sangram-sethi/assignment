import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { LayoutStore } from './layout.store';
import { Sidebar } from './sidebar';
import { Topbar } from './topbar';
import { CommandPalette } from '../shared/components/command-palette';

@Component({
  selector: 'app-shell',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterOutlet, Sidebar, Topbar, CommandPalette],
  template: `
    <div class="shell" [class.collapsed]="layout.collapsed()">
      <app-sidebar />
      <div class="scrim" [class.show]="layout.mobileOpen()" (click)="layout.closeMobile()"></div>
      <div class="main">
        <app-topbar />
        <main class="content">
          <router-outlet />
        </main>
      </div>
      <app-command-palette />
    </div>
  `,
  styles: [
    `
      .main {
        margin-left: 266px;
        min-height: 100dvh;
        transition: margin-left 0.45s cubic-bezier(0.16, 1, 0.3, 1);
      }
      .shell.collapsed .main {
        margin-left: 84px;
      }
      .content {
        padding: clamp(1rem, 3vw, 1.9rem);
        max-width: 1360px;
        margin: 0 auto;
      }
      .scrim {
        position: fixed;
        inset: 0;
        z-index: 55;
        background: rgba(4, 6, 18, 0.55);
        backdrop-filter: blur(4px);
        opacity: 0;
        pointer-events: none;
        transition: opacity 0.3s ease;
      }
      .scrim.show {
        opacity: 1;
        pointer-events: auto;
      }
      @media (max-width: 1024px) {
        .main {
          margin-left: 0 !important;
        }
      }
    `,
  ],
})
export class Shell {
  readonly layout = inject(LayoutStore);
}

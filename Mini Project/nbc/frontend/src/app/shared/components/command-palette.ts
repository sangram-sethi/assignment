import {
  ChangeDetectionStrategy,
  Component,
  ElementRef,
  computed,
  effect,
  inject,
  signal,
  viewChild,
} from '@angular/core';
import { Router } from '@angular/router';
import { CommandPaletteService } from '../../core/services/command-palette.service';
import { AuthService } from '../../core/services/auth.service';
import { ThemeService } from '../../core/services/theme.service';
import { Icon } from './icon';

interface Command {
  id: string;
  label: string;
  icon: string;
  hint?: string;
  keywords?: string;
  run: () => void;
}

@Component({
  selector: 'app-command-palette',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [Icon],
  host: {
    '(document:keydown)': 'handleKey($event)',
  },
  template: `
    @if (cp.open()) {
      <div class="cmdk" (click)="onBackdrop($event)">
        <div class="panel" animate.enter="pop" role="dialog" aria-label="Command palette">
          <div class="search">
            <app-icon name="search" [size]="18" />
            <input
              #q
              type="text"
              [value]="query()"
              (input)="onInput($event)"
              placeholder="Search pages, actions…"
              autocomplete="off"
              spellcheck="false"
            />
            <span class="kbd">Esc</span>
          </div>
          <div class="results no-scrollbar">
            @for (c of filtered(); track c.id; let i = $index) {
              <button
                type="button"
                class="row"
                [class.active]="i === selected()"
                (click)="run(c)"
                (mouseenter)="selected.set(i)"
              >
                <span class="ic"><app-icon [name]="c.icon" [size]="17" /></span>
                <span class="label">{{ c.label }}</span>
                @if (c.hint) { <span class="hint">{{ c.hint }}</span> }
                <app-icon name="arrowRight" [size]="15" class="go" />
              </button>
            } @empty {
              <div class="empty">No matches for “{{ query() }}”</div>
            }
          </div>
          <div class="foot">
            <span><span class="kbd">↑</span><span class="kbd">↓</span> navigate</span>
            <span><span class="kbd">↵</span> select</span>
            <span class="spacer"></span>
            <span class="brand">Aurora ⌘K</span>
          </div>
        </div>
      </div>
    }
  `,
  styles: [
    `
      .cmdk {
        position: fixed;
        inset: 0;
        z-index: 1500;
        display: grid;
        place-items: start center;
        padding-top: 14vh;
        background: rgba(4, 6, 18, 0.55);
        backdrop-filter: blur(8px);
        animation: fade 0.2s ease;
      }
      @keyframes fade { from { opacity: 0; } to { opacity: 1; } }
      .panel {
        width: min(94vw, 620px);
        border-radius: 1.4rem;
        overflow: hidden;
        background: var(--surface-2);
        border: 1px solid var(--line-strong);
        backdrop-filter: blur(30px) saturate(160%);
        box-shadow: var(--shadow-2);
      }
      .pop { animation: cmd-pop 0.32s cubic-bezier(0.16, 1, 0.3, 1); }
      @keyframes cmd-pop {
        from { opacity: 0; transform: translateY(-10px) scale(0.98); }
        to { opacity: 1; transform: translateY(0) scale(1); }
      }
      .search {
        display: flex;
        align-items: center;
        gap: 0.75rem;
        padding: 1.05rem 1.15rem;
        border-bottom: 1px solid var(--line);
        color: var(--muted);
      }
      .search input {
        flex: 1;
        background: transparent;
        border: none;
        outline: none;
        color: var(--ink);
        font: inherit;
        font-size: 1.02rem;
      }
      .search input::placeholder { color: var(--faint); }
      .results { max-height: 46vh; overflow-y: auto; padding: 0.5rem; }
      .row {
        width: 100%;
        display: flex;
        align-items: center;
        gap: 0.8rem;
        padding: 0.7rem 0.8rem;
        border-radius: 0.85rem;
        background: transparent;
        border: none;
        cursor: pointer;
        color: var(--muted);
        text-align: left;
        transition: background 0.15s ease, color 0.15s ease, transform 0.15s ease;
      }
      .row .go { margin-left: auto; opacity: 0; transform: translateX(-4px); transition: all 0.2s ease; }
      .row.active {
        background: var(--surface);
        color: var(--ink);
      }
      .row.active .go { opacity: 1; transform: translateX(0); color: var(--violet); }
      .ic {
        display: grid;
        place-items: center;
        width: 34px;
        height: 34px;
        border-radius: 0.65rem;
        background: var(--surface);
        border: 1px solid var(--line);
        color: var(--ink);
      }
      .row.active .ic { border-color: var(--ring); box-shadow: 0 0 0 3px var(--ring-soft); }
      .label { font-weight: 600; font-size: 0.95rem; }
      .hint { margin-left: auto; font-size: 0.75rem; color: var(--faint); }
      .row.active .hint { margin-left: 0.5rem; }
      .empty { padding: 2rem; text-align: center; color: var(--faint); font-size: 0.9rem; }
      .foot {
        display: flex;
        align-items: center;
        gap: 1rem;
        padding: 0.7rem 1.1rem;
        border-top: 1px solid var(--line);
        font-size: 0.74rem;
        color: var(--faint);
      }
      .foot .spacer { flex: 1; }
      .foot .brand { font-family: var(--font-mono); letter-spacing: 0.08em; }
    `,
  ],
})
export class CommandPalette {
  readonly cp = inject(CommandPaletteService);
  private readonly router = inject(Router);
  private readonly auth = inject(AuthService);
  private readonly theme = inject(ThemeService);

  private readonly input = viewChild<ElementRef<HTMLInputElement>>('q');
  readonly query = signal('');
  readonly selected = signal(0);

  private readonly commands = computed<Command[]>(() => {
    const go = (path: string) => () => this.navigate(path);
    const list: Command[] = [];
    if (this.auth.isApprover()) {
      list.push({ id: 'approvals', label: 'Approval Queue', icon: 'shieldCheck', hint: 'Page', keywords: 'pending review', run: go('/approvals') });
    } else {
      list.push(
        { id: 'dashboard', label: 'Dashboard', icon: 'grid', hint: 'Page', keywords: 'home overview', run: go('/dashboard') },
        { id: 'apply', label: 'Apply for a Loan', icon: 'plus', hint: 'Page', keywords: 'new loan request', run: go('/apply') },
        { id: 'calculator', label: 'EMI Calculator', icon: 'calculator', hint: 'Page', keywords: 'emi interest quote', run: go('/calculator') },
        { id: 'loans', label: 'My Loans', icon: 'list', hint: 'Page', keywords: 'applications history', run: go('/loans') },
      );
    }
    list.push(
      { id: 'profile', label: 'Profile', icon: 'user', hint: 'Page', keywords: 'account settings me', run: go('/profile') },
      { id: 'theme', label: `Switch to ${this.theme.theme() === 'dark' ? 'light' : 'dark'} theme`, icon: this.theme.theme() === 'dark' ? 'sun' : 'moon', hint: 'Action', keywords: 'appearance mode toggle', run: () => this.theme.toggle() },
      { id: 'logout', label: 'Sign out', icon: 'logout', hint: 'Action', keywords: 'exit quit logout', run: () => { this.cp.hide(); this.auth.logout(); } },
    );
    return list;
  });

  readonly filtered = computed<Command[]>(() => {
    const q = this.query().trim().toLowerCase();
    if (!q) return this.commands();
    return this.commands().filter((c) =>
      (c.label + ' ' + (c.keywords ?? '') + ' ' + (c.hint ?? '')).toLowerCase().includes(q),
    );
  });

  constructor() {
    effect(() => {
      if (this.cp.open()) {
        this.query.set('');
        this.selected.set(0);
        setTimeout(() => this.input()?.nativeElement.focus(), 30);
      }
    });
  }

  onInput(e: Event): void {
    this.query.set((e.target as HTMLInputElement).value);
    this.selected.set(0);
  }

  run(c: Command): void {
    this.cp.hide();
    c.run();
  }

  private navigate(path: string): void {
    void this.router.navigateByUrl(path);
  }

  onBackdrop(e: MouseEvent): void {
    if ((e.target as HTMLElement).classList.contains('cmdk')) this.cp.hide();
  }

  handleKey(e: KeyboardEvent): boolean {
    const isToggle = (e.key === 'k' || e.key === 'K') && (e.metaKey || e.ctrlKey);
    if (isToggle) {
      e.preventDefault();
      this.cp.toggle();
      return true;
    }
    if (!this.cp.open()) return false;
    const items = this.filtered();
    if (e.key === 'Escape') {
      e.preventDefault();
      this.cp.hide();
    } else if (e.key === 'ArrowDown') {
      e.preventDefault();
      this.selected.set((this.selected() + 1) % Math.max(items.length, 1));
    } else if (e.key === 'ArrowUp') {
      e.preventDefault();
      this.selected.set((this.selected() - 1 + items.length) % Math.max(items.length, 1));
    } else if (e.key === 'Enter') {
      e.preventDefault();
      const c = items[this.selected()];
      if (c) this.run(c);
    }
    return true;
  }
}

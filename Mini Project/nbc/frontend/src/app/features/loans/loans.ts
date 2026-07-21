import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { catchError, of } from 'rxjs';
import { LoanService } from '../../core/services/loan.service';
import { LoanApplicationResponse, LoanStatus } from '../../core/models';
import { PageHeader } from '../../shared/components/page-header';
import { LoanListItem } from '../../shared/components/loan-list-item';
import { EmptyState } from '../../shared/components/empty-state';
import { Icon } from '../../shared/components/icon';

type Filter = 'ALL' | LoanStatus;

@Component({
  selector: 'app-loans',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, PageHeader, LoanListItem, EmptyState, Icon],
  template: `
    <app-page-header
      eyebrow="Portfolio"
      title="My Loans"
      subtitle="Every application you've made, all in one place."
    >
      <a class="btn btn-primary" routerLink="/apply" ripple magnetic>
        <app-icon name="plus" [size]="18" /> New application
      </a>
    </app-page-header>

    <div class="tabs" role="tablist">
      @for (t of tabs; track t.key) {
        <button
          type="button"
          class="tab"
          [class.on]="filter() === t.key"
          (click)="filter.set(t.key)"
        >
          {{ t.label }}
          <span class="cnt">{{ count(t.key) }}</span>
        </button>
      }
    </div>

    @if (loading()) {
      <div class="flex flex-col gap-3 mt-4">
        @for (i of [1, 2, 3, 4]; track i) {
          <div class="skeleton" style="height: 82px"></div>
        }
      </div>
    } @else if (filtered().length) {
      <div class="flex flex-col gap-3 mt-4">
        @for (loan of filtered(); track loan.id) {
          <app-loan-list-item [loan]="loan" />
        }
      </div>
    } @else {
      <div class="card mt-4">
        <app-empty-state
          [icon]="loans().length ? 'filter' : 'layers'"
          [title]="loans().length ? 'Nothing here' : 'No loans yet'"
          [sub]="
            loans().length
              ? 'No applications match this filter. Try another tab.'
              : 'Apply for your first loan to get started.'
          "
        >
          @if (!loans().length) {
            <a class="btn btn-primary" routerLink="/apply" ripple>
              <app-icon name="plus" [size]="18" /> Apply now
            </a>
          }
        </app-empty-state>
      </div>
    }
  `,
  styles: [
    `
      .tabs {
        display: inline-flex;
        gap: 0.25rem;
        padding: 0.35rem;
        border-radius: 1rem;
        background: var(--surface);
        border: 1px solid var(--line);
        flex-wrap: wrap;
      }
      .tab {
        display: inline-flex;
        align-items: center;
        gap: 0.5rem;
        padding: 0.5rem 0.9rem;
        border-radius: 0.7rem;
        background: transparent;
        border: none;
        color: var(--muted);
        font-weight: 600;
        font-size: 0.88rem;
        cursor: pointer;
        transition: color 0.2s ease, background 0.25s ease;
      }
      .tab:hover {
        color: var(--ink);
      }
      .tab.on {
        color: #fff;
        background: var(--grad-brand);
        box-shadow: 0 8px 20px -8px var(--glow);
      }
      .cnt {
        font-size: 0.72rem;
        font-weight: 700;
        padding: 0.1rem 0.45rem;
        border-radius: 999px;
        background: var(--surface-2);
        color: var(--muted);
      }
      .tab.on .cnt {
        background: rgba(255, 255, 255, 0.25);
        color: #fff;
      }
    `,
  ],
})
export class Loans {
  private readonly loansApi = inject(LoanService);

  readonly loading = signal(true);
  readonly loans = signal<LoanApplicationResponse[]>([]);
  readonly filter = signal<Filter>('ALL');

  readonly tabs: { key: Filter; label: string }[] = [
    { key: 'ALL', label: 'All' },
    { key: 'PENDING', label: 'Pending' },
    { key: 'APPROVED', label: 'Approved' },
    { key: 'REJECTED', label: 'Rejected' },
  ];

  readonly sorted = computed(() =>
    [...this.loans()].sort(
      (a, b) => +new Date(b.applicationDate) - +new Date(a.applicationDate),
    ),
  );

  readonly filtered = computed(() => {
    const f = this.filter();
    return f === 'ALL' ? this.sorted() : this.sorted().filter((l) => l.status === f);
  });

  constructor() {
    this.loansApi
      .getMyLoans()
      .pipe(catchError(() => of([] as LoanApplicationResponse[])))
      .subscribe((list) => {
        this.loans.set(list);
        this.loading.set(false);
      });
  }

  count(key: Filter): number {
    return key === 'ALL'
      ? this.loans().length
      : this.loans().filter((l) => l.status === key).length;
  }
}

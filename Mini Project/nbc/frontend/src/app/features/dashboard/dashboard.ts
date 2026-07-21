import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { catchError, forkJoin, of } from 'rxjs';
import { CustomerService } from '../../core/services/customer.service';
import { LoanService } from '../../core/services/loan.service';
import { AuthService } from '../../core/services/auth.service';
import { CustomerResponse, LoanApplicationResponse, LoanSummaryResponse } from '../../core/models';
import { PageHeader } from '../../shared/components/page-header';
import { StatCard } from '../../shared/components/stat-card';
import { DonutChart, DonutSegment } from '../../shared/components/donut-chart';
import { LoanListItem } from '../../shared/components/loan-list-item';
import { EmptyState } from '../../shared/components/empty-state';
import { Icon } from '../../shared/components/icon';
import { Reveal, Magnetic, Ripple } from '../../shared/directives';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    RouterLink,
    PageHeader,
    StatCard,
    DonutChart,
    LoanListItem,
    EmptyState,
    Icon,
    Reveal,
    Magnetic,
    Ripple,
  ],
  templateUrl: './dashboard.html',
})
export class Dashboard {
  private readonly customers = inject(CustomerService);
  private readonly loansApi = inject(LoanService);
  private readonly auth = inject(AuthService);

  readonly loading = signal(true);
  readonly profile = signal<CustomerResponse | null>(null);
  readonly summary = signal<LoanSummaryResponse | null>(null);
  readonly loans = signal<LoanApplicationResponse[]>([]);

  readonly firstName = computed(() => {
    const n = this.profile()?.name || this.auth.username();
    return n.split(/\s+/)[0] || 'there';
  });

  readonly greeting = computed(() => {
    const h = new Date().getHours();
    const g = h < 12 ? 'Good morning' : h < 17 ? 'Good afternoon' : 'Good evening';
    return `${g}, ${this.firstName()}`;
  });

  readonly recent = computed(() =>
    [...this.loans()]
      .sort((a, b) => +new Date(b.applicationDate) - +new Date(a.applicationDate))
      .slice(0, 5),
  );

  readonly approvedAmount = computed(() =>
    this.loans()
      .filter((l) => l.status === 'APPROVED')
      .reduce((sum, l) => sum + Number(l.loanAmount), 0),
  );

  readonly totalEmi = computed(() =>
    this.loans()
      .filter((l) => l.status === 'APPROVED')
      .reduce((sum, l) => sum + Number(l.monthlyEmi), 0),
  );

  readonly segments = computed<DonutSegment[]>(() => {
    const s = this.summary();
    if (!s) return [];
    return [
      { label: 'Approved', value: s.approvedApplications, color: '#34d399' },
      { label: 'Pending', value: s.pendingApplications, color: '#fbbf24' },
      { label: 'Rejected', value: s.rejectedApplications, color: '#fb7185' },
    ].filter((x) => x.value > 0);
  });

  readonly hasAny = computed(() => (this.summary()?.totalApplications ?? 0) > 0);

  constructor() {
    this.load();
  }

  private load(): void {
    this.loading.set(true);
    forkJoin({
      profile: this.customers.getProfile().pipe(catchError(() => of(null))),
      summary: this.customers.getLoanSummary().pipe(catchError(() => of(null))),
      loans: this.loansApi.getMyLoans().pipe(catchError(() => of([] as LoanApplicationResponse[]))),
    }).subscribe((res) => {
      this.profile.set(res.profile);
      this.summary.set(
        res.summary ?? {
          totalApplications: 0,
          approvedApplications: 0,
          rejectedApplications: 0,
          pendingApplications: 0,
        },
      );
      this.loans.set(res.loans);
      this.loading.set(false);
    });
  }
}

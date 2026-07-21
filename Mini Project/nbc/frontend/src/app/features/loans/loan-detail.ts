import {
  ChangeDetectionStrategy,
  Component,
  computed,
  effect,
  inject,
  input,
  signal,
} from '@angular/core';
import { RouterLink } from '@angular/router';
import { catchError, forkJoin, of } from 'rxjs';
import { LoanService } from '../../core/services/loan.service';
import { CustomerService } from '../../core/services/customer.service';
import { ToastService } from '../../core/services/toast.service';
import { LoanApplicationResponse } from '../../core/models';
import { loanMeta } from '../../core/loan-meta';
import { amortizationSchedule } from '../../shared/finance';
import { formatCompactINR } from '../../shared/format';
import { exportLoanPdf } from '../../shared/pdf';
import { DonutChart, DonutSegment } from '../../shared/components/donut-chart';
import { StatusBadge } from '../../shared/components/status-badge';
import { EmptyState } from '../../shared/components/empty-state';
import { Icon } from '../../shared/components/icon';
import { Reveal } from '../../shared/directives';
import { InrPipe, DateTimePipe } from '../../shared/pipes';

@Component({
  selector: 'app-loan-detail',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    RouterLink,
    DonutChart,
    StatusBadge,
    EmptyState,
    Icon,
    Reveal,
    InrPipe,
    DateTimePipe,
  ],
  templateUrl: './loan-detail.html',
  styleUrl: './loan-detail.scss',
})
export class LoanDetail {
  private readonly loansApi = inject(LoanService);
  private readonly customers = inject(CustomerService);
  private readonly toast = inject(ToastService);

  readonly id = input<string>();

  readonly loading = signal(true);
  readonly notFound = signal(false);
  readonly loan = signal<LoanApplicationResponse | null>(null);
  readonly applicant = signal<string>('');
  readonly showAll = signal(false);

  readonly meta = computed(() => (this.loan() ? loanMeta(this.loan()!.loanType) : null));

  readonly segments = computed<DonutSegment[]>(() => {
    const l = this.loan();
    if (!l) return [];
    return [
      { label: 'Principal', value: Number(l.loanAmount), color: '#7c5cff' },
      { label: 'Interest', value: Number(l.totalInterest), color: '#38bdf8' },
    ];
  });

  readonly totalPayableCompact = computed(() =>
    this.loan() ? formatCompactINR(this.loan()!.totalPayable) : '',
  );

  readonly schedule = computed(() => {
    const l = this.loan();
    if (!l) return [];
    return amortizationSchedule(
      Number(l.loanAmount),
      Number(l.interestRate),
      Number(l.tenureMonths),
    );
  });

  readonly visibleRows = computed(() =>
    this.showAll() ? this.schedule() : this.schedule().slice(0, 12),
  );

  constructor() {
    effect(() => {
      const idStr = this.id();
      if (!idStr) return;
      this.fetch(Number(idStr));
    });
  }

  private fetch(id: number): void {
    this.loading.set(true);
    this.notFound.set(false);
    forkJoin({
      loan: this.loansApi.getLoan(id).pipe(catchError(() => of(null))),
      profile: this.customers.getProfile().pipe(catchError(() => of(null))),
    }).subscribe((res) => {
      if (!res.loan) {
        this.notFound.set(true);
        this.loan.set(null);
      } else {
        this.loan.set(res.loan);
        this.applicant.set(res.profile?.name ?? '');
      }
      this.loading.set(false);
    });
  }

  exportPdf(): void {
    const l = this.loan();
    if (!l) return;
    exportLoanPdf(l, this.applicant());
    this.toast.success('Statement downloaded', `Aurora-Loan-${l.id}.pdf saved.`);
  }
}

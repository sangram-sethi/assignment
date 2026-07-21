import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { LoanService } from '../../core/services/loan.service';
import { ToastService } from '../../core/services/toast.service';
import { ConfettiService } from '../../core/services/confetti.service';
import { LoanType } from '../../core/models';
import { LOAN_TYPE_LIST, loanMeta } from '../../core/loan-meta';
import { emiTotals } from '../../shared/finance';
import { formatCompactINR } from '../../shared/format';
import { apiErrorMessage } from '../../shared/http-error';
import { PageHeader } from '../../shared/components/page-header';
import { DonutChart, DonutSegment } from '../../shared/components/donut-chart';
import { Icon } from '../../shared/components/icon';
import { Reveal, Magnetic, Ripple } from '../../shared/directives';
import { InrPipe } from '../../shared/pipes';

const AMOUNT_MIN = 50_000;
const AMOUNT_MAX = 10_000_000;
const TENURE_MIN = 6;
const TENURE_MAX = 360;
const VALID_TYPES: LoanType[] = [
  'HOME_LOAN',
  'PERSONAL_LOAN',
  'VEHICLE_LOAN',
  'EDUCATION_LOAN',
  'BUSINESS_LOAN',
];

@Component({
  selector: 'app-apply',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, PageHeader, DonutChart, Icon, Reveal, Magnetic, Ripple, InrPipe],
  templateUrl: './apply.html',
  styleUrl: './apply.scss',
})
export class Apply {
  private readonly loansApi = inject(LoanService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly toast = inject(ToastService);
  private readonly confetti = inject(ConfettiService);

  readonly types = LOAN_TYPE_LIST;

  readonly loanType = signal<LoanType>('HOME_LOAN');
  readonly amount = signal(500_000);
  readonly tenure = signal(60);
  readonly purpose = signal('');
  readonly submitting = signal(false);
  readonly tried = signal(false);
  readonly error = signal<string | null>(null);

  readonly rate = computed(() => loanMeta(this.loanType()).rate);
  readonly result = computed(() => emiTotals(this.amount(), this.rate(), this.tenure()));
  readonly amountPct = computed(
    () => ((this.amount() - AMOUNT_MIN) / (AMOUNT_MAX - AMOUNT_MIN)) * 100,
  );
  readonly tenurePct = computed(
    () => ((this.tenure() - TENURE_MIN) / (TENURE_MAX - TENURE_MIN)) * 100,
  );
  readonly totalPayableCompact = computed(() => formatCompactINR(this.result().totalPayable));
  readonly tenureYears = computed(() => (this.tenure() / 12).toFixed(this.tenure() % 12 ? 1 : 0));
  readonly segments = computed<DonutSegment[]>(() => [
    { label: 'Principal', value: this.amount(), color: '#7c5cff' },
    { label: 'Interest', value: this.result().totalInterest, color: '#38bdf8' },
  ]);
  readonly purposeInvalid = computed(() => this.tried() && this.purpose().trim().length < 3);

  constructor() {
    const q = this.route.snapshot.queryParamMap;
    const t = q.get('type') as LoanType | null;
    if (t && VALID_TYPES.includes(t)) this.loanType.set(t);
    const a = Number(q.get('amount'));
    if (a) this.amount.set(clamp(a, AMOUNT_MIN, AMOUNT_MAX));
    const tn = Number(q.get('tenure'));
    if (tn) this.tenure.set(clamp(tn, TENURE_MIN, TENURE_MAX));
  }

  setAmount(e: Event): void {
    const v = Number((e.target as HTMLInputElement).value);
    if (!Number.isNaN(v)) this.amount.set(clamp(v, AMOUNT_MIN, AMOUNT_MAX));
  }
  setTenure(e: Event): void {
    const v = Number((e.target as HTMLInputElement).value);
    if (!Number.isNaN(v)) this.tenure.set(clamp(v, TENURE_MIN, TENURE_MAX));
  }
  setPurpose(e: Event): void {
    this.purpose.set((e.target as HTMLTextAreaElement).value);
  }

  submit(): void {
    this.tried.set(true);
    this.error.set(null);
    if (this.purpose().trim().length < 3) return;
    this.submitting.set(true);
    this.loansApi
      .apply({
        loanType: this.loanType(),
        loanAmount: this.amount(),
        tenureMonths: this.tenure(),
        purpose: this.purpose().trim(),
      })
      .subscribe({
        next: (loan) => {
          this.submitting.set(false);
          this.confetti.cannons();
          this.toast.success('Application submitted', 'Your loan is now pending review.');
          void this.router.navigate(['/loans', loan.id]);
        },
        error: (err) => {
          this.submitting.set(false);
          this.error.set(apiErrorMessage(err, 'Could not submit your application.'));
        },
      });
  }
}

function clamp(v: number, min: number, max: number): number {
  return Math.min(max, Math.max(min, v));
}

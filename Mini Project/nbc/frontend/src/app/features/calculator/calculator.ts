import { ChangeDetectionStrategy, Component, computed, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { LoanType } from '../../core/models';
import { LOAN_TYPE_LIST, loanMeta } from '../../core/loan-meta';
import { emiTotals } from '../../shared/finance';
import { formatCompactINR } from '../../shared/format';
import { PageHeader } from '../../shared/components/page-header';
import { DonutChart, DonutSegment } from '../../shared/components/donut-chart';
import { Icon } from '../../shared/components/icon';
import { Reveal, Magnetic, Ripple } from '../../shared/directives';
import { InrPipe } from '../../shared/pipes';

const AMOUNT_MIN = 50_000;
const AMOUNT_MAX = 10_000_000;
const TENURE_MIN = 6;
const TENURE_MAX = 360;

@Component({
  selector: 'app-calculator',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, PageHeader, DonutChart, Icon, Reveal, Magnetic, Ripple, InrPipe],
  templateUrl: './calculator.html',
  styleUrl: './calculator.scss',
})
export class Calculator {
  readonly types = LOAN_TYPE_LIST;

  readonly loanType = signal<LoanType>('HOME_LOAN');
  readonly amount = signal(500_000);
  readonly tenure = signal(60);

  readonly rate = computed(() => loanMeta(this.loanType()).rate);
  readonly result = computed(() => emiTotals(this.amount(), this.rate(), this.tenure()));

  readonly amountPct = computed(
    () => ((this.amount() - AMOUNT_MIN) / (AMOUNT_MAX - AMOUNT_MIN)) * 100,
  );
  readonly tenurePct = computed(
    () => ((this.tenure() - TENURE_MIN) / (TENURE_MAX - TENURE_MIN)) * 100,
  );

  readonly totalPayableCompact = computed(() => formatCompactINR(this.result().totalPayable));

  readonly segments = computed<DonutSegment[]>(() => [
    { label: 'Principal', value: this.amount(), color: '#7c5cff' },
    { label: 'Interest', value: this.result().totalInterest, color: '#38bdf8' },
  ]);

  readonly tenureYears = computed(() => (this.tenure() / 12).toFixed(this.tenure() % 12 ? 1 : 0));

  setAmount(e: Event): void {
    const v = Number((e.target as HTMLInputElement).value);
    if (!Number.isNaN(v)) this.amount.set(clamp(v, AMOUNT_MIN, AMOUNT_MAX));
  }
  setTenure(e: Event): void {
    const v = Number((e.target as HTMLInputElement).value);
    if (!Number.isNaN(v)) this.tenure.set(clamp(v, TENURE_MIN, TENURE_MAX));
  }
}

function clamp(v: number, min: number, max: number): number {
  return Math.min(max, Math.max(min, v));
}

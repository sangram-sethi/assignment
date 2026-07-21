import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { catchError, of } from 'rxjs';
import { ApprovalService } from '../../core/services/approval.service';
import { ToastService } from '../../core/services/toast.service';
import { ConfettiService } from '../../core/services/confetti.service';
import { LoanApplicationResponse } from '../../core/models';
import { loanMeta } from '../../core/loan-meta';
import { apiErrorMessage } from '../../shared/http-error';
import { PageHeader } from '../../shared/components/page-header';
import { EmptyState } from '../../shared/components/empty-state';
import { Modal } from '../../shared/components/modal';
import { Icon } from '../../shared/components/icon';
import { Reveal } from '../../shared/directives';
import { InrPipe, ShortDatePipe } from '../../shared/pipes';

type Decision = 'APPROVED' | 'REJECTED';

@Component({
  selector: 'app-approvals',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [PageHeader, EmptyState, Modal, Icon, Reveal, InrPipe, ShortDatePipe],
  templateUrl: './approvals.html',
  styleUrl: './approvals.scss',
})
export class Approvals {
  private readonly approvals = inject(ApprovalService);
  private readonly toast = inject(ToastService);
  private readonly confetti = inject(ConfettiService);

  readonly loading = signal(true);
  readonly pending = signal<LoanApplicationResponse[]>([]);

  readonly decisionLoan = signal<LoanApplicationResponse | null>(null);
  readonly decisionType = signal<Decision>('APPROVED');
  readonly remarks = signal('');
  readonly processing = signal(false);
  readonly tried = signal(false);

  readonly totalValue = computed(() =>
    this.pending().reduce((sum, l) => sum + Number(l.loanAmount), 0),
  );

  meta(l: LoanApplicationResponse) {
    return loanMeta(l.loanType);
  }

  constructor() {
    this.load();
  }

  private load(): void {
    this.loading.set(true);
    this.approvals
      .getPending()
      .pipe(catchError(() => of([] as LoanApplicationResponse[])))
      .subscribe((list) => {
        this.pending.set(
          [...list].sort((a, b) => +new Date(a.applicationDate) - +new Date(b.applicationDate)),
        );
        this.loading.set(false);
      });
  }

  openDecision(loan: LoanApplicationResponse, type: Decision): void {
    this.decisionLoan.set(loan);
    this.decisionType.set(type);
    this.remarks.set('');
    this.tried.set(false);
  }

  closeDecision(): void {
    if (this.processing()) return;
    this.decisionLoan.set(null);
  }

  setRemarks(e: Event): void {
    this.remarks.set((e.target as HTMLTextAreaElement).value);
  }

  confirm(): void {
    this.tried.set(true);
    const loan = this.decisionLoan();
    if (!loan || this.remarks().trim().length < 3) return;
    const type = this.decisionType();
    this.processing.set(true);
    this.approvals.decide(loan.id, { status: type, remarks: this.remarks().trim() }).subscribe({
      next: () => {
        this.processing.set(false);
        this.pending.update((list) => list.filter((l) => l.id !== loan.id));
        this.decisionLoan.set(null);
        if (type === 'APPROVED') {
          this.confetti.cannons();
          this.toast.success('Loan approved', `Application #${loan.id} has been approved.`);
        } else {
          this.toast.info('Loan rejected', `Application #${loan.id} has been rejected.`);
        }
      },
      error: (err) => {
        this.processing.set(false);
        this.toast.error('Could not process', apiErrorMessage(err));
      },
    });
  }
}

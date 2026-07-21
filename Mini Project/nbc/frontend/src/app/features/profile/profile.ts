import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { catchError, forkJoin, of } from 'rxjs';
import { AuthService } from '../../core/services/auth.service';
import { CustomerService } from '../../core/services/customer.service';
import { ThemeService } from '../../core/services/theme.service';
import { CustomerResponse, LoanSummaryResponse } from '../../core/models';
import { initials } from '../../shared/format';
import { PageHeader } from '../../shared/components/page-header';
import { Icon } from '../../shared/components/icon';
import { Reveal } from '../../shared/directives';
import { InrPipe } from '../../shared/pipes';

@Component({
  selector: 'app-profile',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [PageHeader, Icon, Reveal, InrPipe],
  templateUrl: './profile.html',
  styleUrl: './profile.scss',
})
export class Profile {
  readonly auth = inject(AuthService);
  readonly theme = inject(ThemeService);
  private readonly customers = inject(CustomerService);

  readonly loading = signal(true);
  readonly profile = signal<CustomerResponse | null>(null);
  readonly summary = signal<LoanSummaryResponse | null>(null);

  readonly isCustomer = this.auth.isCustomer;
  readonly roleLabel = computed(() => (this.auth.isApprover() ? 'Loan Approver' : 'Customer'));
  readonly displayName = computed(() => this.profile()?.name || this.auth.username());
  readonly avatarText = computed(() => initials(this.displayName()).toUpperCase());

  readonly details = computed(() => {
    const p = this.profile();
    if (!p) return [];
    return [
      { icon: 'mail', label: 'Email', value: p.email },
      { icon: 'phone', label: 'Phone', value: p.phone },
    ];
  });

  constructor() {
    if (this.auth.isCustomer()) {
      forkJoin({
        profile: this.customers.getProfile().pipe(catchError(() => of(null))),
        summary: this.customers.getLoanSummary().pipe(catchError(() => of(null))),
      }).subscribe((res) => {
        this.profile.set(res.profile);
        this.summary.set(res.summary);
        this.loading.set(false);
      });
    } else {
      this.loading.set(false);
    }
  }
}

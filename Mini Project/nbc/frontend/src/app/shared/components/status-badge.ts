import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';
import { LoanStatus } from '../../core/models';
import { statusMeta } from '../../core/loan-meta';
import { Icon } from './icon';

@Component({
  selector: 'app-status-badge',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [Icon],
  template: `
    <span class="badge" [class]="meta().className">
      <app-icon [name]="meta().icon" [size]="13" [strokeWidth]="2.4" />
      {{ meta().label }}
    </span>
  `,
})
export class StatusBadge {
  readonly status = input.required<LoanStatus>();
  readonly meta = computed(() => statusMeta(this.status()));
}

import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { LoanApplicationResponse } from '../../core/models';
import { loanMeta } from '../../core/loan-meta';
import { Icon } from './icon';
import { StatusBadge } from './status-badge';
import { InrPipe, ShortDatePipe } from '../pipes';
import { Reveal } from '../directives';

@Component({
  selector: 'app-loan-list-item',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, Icon, StatusBadge, InrPipe, ShortDatePipe, Reveal],
  template: `
    <a class="li card card-hover" [routerLink]="['/loans', loan().id]" reveal="up" [revealDistance]="14">
      <span class="ic" [style.background]="meta().gradient">
        <app-icon [name]="meta().icon" [size]="20" />
      </span>
      <div class="info">
        <p class="type">{{ meta().label }}</p>
        <p class="purpose">{{ loan().purpose }}</p>
      </div>
      <div class="amt">
        <p class="a tabular">{{ loan().loanAmount | inr }}</p>
        <p class="e tabular">EMI {{ loan().monthlyEmi | inr }}</p>
      </div>
      <div class="meta-col">
        <app-status-badge [status]="loan().status" />
        <span class="date">{{ loan().applicationDate | shortDate }}</span>
      </div>
      <app-icon name="chevronRight" [size]="18" class="chev" />
    </a>
  `,
  styles: [
    `
      .li {
        display: flex;
        align-items: center;
        gap: 1rem;
        padding: 1rem 1.15rem;
        text-decoration: none;
      }
      .ic {
        flex: none;
        display: grid;
        place-items: center;
        width: 48px;
        height: 48px;
        border-radius: 0.95rem;
        color: #fff;
        box-shadow: 0 10px 24px -12px rgba(0, 0, 0, 0.6);
      }
      .info {
        flex: 1;
        min-width: 0;
      }
      .type {
        margin: 0;
        font-weight: 700;
        color: var(--ink);
        font-size: 0.96rem;
      }
      .purpose {
        margin: 0.2rem 0 0;
        font-size: 0.83rem;
        color: var(--muted);
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
        max-width: 26ch;
      }
      .amt {
        text-align: right;
        flex: none;
      }
      .amt .a {
        margin: 0;
        font-weight: 800;
        color: var(--ink);
        font-size: 0.98rem;
      }
      .amt .e {
        margin: 0.2rem 0 0;
        font-size: 0.78rem;
        color: var(--faint);
      }
      .meta-col {
        display: flex;
        flex-direction: column;
        align-items: flex-end;
        gap: 0.4rem;
        flex: none;
        min-width: 108px;
      }
      .date {
        font-size: 0.74rem;
        color: var(--faint);
      }
      .chev {
        color: var(--faint);
        flex: none;
        transition: transform 0.25s ease, color 0.25s ease;
      }
      .li:hover .chev {
        color: var(--violet);
        transform: translateX(3px);
      }
      @media (max-width: 640px) {
        .amt,
        .date {
          display: none;
        }
        .purpose {
          max-width: 18ch;
        }
      }
    `,
  ],
})
export class LoanListItem {
  readonly loan = input.required<LoanApplicationResponse>();
  readonly meta = computed(() => loanMeta(this.loan().loanType));
}

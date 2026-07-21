import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { OrderService } from '../../services/order.service';
import { Order } from '../../models/order.model';
import { toUserMessage } from '../../../../core/http/http-error.util';

@Component({
  selector: 'app-order-approval',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './order-approval.component.html',
})
export class OrderApprovalComponent implements OnInit {
  private readonly orders = inject(OrderService);

  private readonly allOrders = signal<Order[]>([]);
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);
  /** Id of the order currently being approved/rejected, to disable its buttons. */
  readonly acting = signal<number | null>(null);

  // The admin queue shows only orders awaiting a decision.
  readonly pendingOrders = computed(() =>
    this.allOrders().filter((o) => o.status === 'PENDING')
  );

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.error.set(null);
    this.orders.getAllOrders().subscribe({
      next: (data) => {
        this.allOrders.set(data);
        this.loading.set(false);
      },
      error: (err: HttpErrorResponse) => {
        this.error.set(toUserMessage(err));
        this.loading.set(false);
      },
    });
  }

  approve(id: number): void {
    this.act(id, this.orders.approveOrder(id));
  }

  reject(id: number): void {
    this.act(id, this.orders.rejectOrder(id));
  }

  private act(id: number, request$: ReturnType<OrderService['approveOrder']>): void {
    this.acting.set(id);
    this.error.set(null);
    request$.subscribe({
      next: (updated) => {
        // Replace the order in place so it drops out of the pending queue.
        this.allOrders.update((orders) =>
          orders.map((o) => (o.orderId === updated.orderId ? updated : o))
        );
        this.acting.set(null);
      },
      error: (err: HttpErrorResponse) => {
        this.error.set(toUserMessage(err));
        this.acting.set(null);
      },
    });
  }
}

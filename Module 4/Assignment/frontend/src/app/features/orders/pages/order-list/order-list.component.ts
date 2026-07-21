import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { OrderService } from '../../services/order.service';
import { Order } from '../../models/order.model';
import { toUserMessage } from '../../../../core/http/http-error.util';

@Component({
  selector: 'app-order-list',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './order-list.component.html',
})
export class OrderListComponent implements OnInit {
  private readonly orders = inject(OrderService);

  readonly myOrders = signal<Order[]>([]);
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);

  ngOnInit(): void {
    this.loading.set(true);
    this.error.set(null);
    this.orders.getMyOrders().subscribe({
      next: (data) => {
        this.myOrders.set(data);
        this.loading.set(false);
      },
      error: (err: HttpErrorResponse) => {
        this.error.set(toUserMessage(err));
        this.loading.set(false);
      },
    });
  }

  badgeClass(status: string): string {
    return 'badge badge-' + status.toLowerCase();
  }
}

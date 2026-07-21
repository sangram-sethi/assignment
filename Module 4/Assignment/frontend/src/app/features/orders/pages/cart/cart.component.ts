import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { CartService } from '../../services/cart.service';
import { OrderService } from '../../services/order.service';
import { toUserMessage } from '../../../../core/http/http-error.util';

@Component({
  selector: 'app-cart',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './cart.component.html',
})
export class CartComponent {
  protected readonly cart = inject(CartService);
  private readonly orders = inject(OrderService);
  private readonly router = inject(Router);

  readonly placing = signal(false);
  readonly error = signal<string | null>(null);

  changeQty(productId: number, quantity: number): void {
    this.cart.setQuantity(productId, quantity);
  }

  remove(productId: number): void {
    this.cart.remove(productId);
  }

  placeOrder(): void {
    if (this.cart.items().length === 0) {
      return;
    }
    this.placing.set(true);
    this.error.set(null);

    const request = {
      orderItems: this.cart.items().map((i) => ({
        productId: i.product.id,
        quantity: i.quantity,
      })),
    };

    this.orders.placeOrder(request).subscribe({
      next: () => {
        this.placing.set(false);
        this.cart.clear();
        this.router.navigate(['/orders/my']);
      },
      error: (err: HttpErrorResponse) => {
        this.placing.set(false);
        this.error.set(toUserMessage(err));
      },
    });
  }
}

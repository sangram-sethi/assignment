import { Injectable, computed, signal } from '@angular/core';
import { Product } from '../../products/models/product.model';

export interface CartItem {
  product: Product;
  quantity: number;
}

/**
 * In-memory shopping cart shared across the app (providedIn: 'root'). Uses
 * signals so the nav badge, cart page and any other consumer stay in sync
 * automatically. The cart is intentionally not persisted — placing an order
 * clears it.
 */
@Injectable({ providedIn: 'root' })
export class CartService {
  private readonly _items = signal<CartItem[]>([]);

  readonly items = this._items.asReadonly();

  /** Total number of units across all lines (used for the nav badge). */
  readonly count = computed(() =>
    this._items().reduce((sum, item) => sum + item.quantity, 0)
  );

  /** Grand total price of the cart. */
  readonly total = computed(() =>
    this._items().reduce((sum, item) => sum + item.quantity * item.product.price, 0)
  );

  add(product: Product, quantity = 1): void {
    this._items.update((items) => {
      const existing = items.find((i) => i.product.id === product.id);
      if (existing) {
        return items.map((i) =>
          i.product.id === product.id ? { ...i, quantity: i.quantity + quantity } : i
        );
      }
      return [...items, { product, quantity }];
    });
  }

  setQuantity(productId: number, quantity: number): void {
    if (quantity <= 0) {
      this.remove(productId);
      return;
    }
    this._items.update((items) =>
      items.map((i) => (i.product.id === productId ? { ...i, quantity } : i))
    );
  }

  remove(productId: number): void {
    this._items.update((items) => items.filter((i) => i.product.id !== productId));
  }

  clear(): void {
    this._items.set([]);
  }
}

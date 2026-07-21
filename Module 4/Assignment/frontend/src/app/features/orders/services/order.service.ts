import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { Order, OrderRequest } from '../models/order.model';

@Injectable({ providedIn: 'root' })
export class OrderService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/orders`;

  // POST /api/orders — customer is derived from the authenticated user.
  placeOrder(request: OrderRequest): Observable<Order> {
    return this.http.post<Order>(this.baseUrl, request);
  }

  // GET /api/orders/my — the current user's own orders.
  getMyOrders(): Observable<Order[]> {
    return this.http.get<Order[]>(`${this.baseUrl}/my`);
  }

  // GET /api/orders — all orders (ADMIN + USER); used by the admin queue.
  getAllOrders(): Observable<Order[]> {
    return this.http.get<Order[]>(this.baseUrl);
  }

  // PATCH /api/orders/{id}/approve  (ADMIN only)
  approveOrder(id: number): Observable<Order> {
    return this.http.patch<Order>(`${this.baseUrl}/${id}/approve`, {});
  }

  // PATCH /api/orders/{id}/reject  (ADMIN only)
  rejectOrder(id: number): Observable<Order> {
    return this.http.patch<Order>(`${this.baseUrl}/${id}/reject`, {});
  }
}

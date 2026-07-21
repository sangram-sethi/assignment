import { Routes } from '@angular/router';
import { roleGuard } from '../../core/auth/role.guard';

export const ORDERS_ROUTES: Routes = [
  {
    path: 'cart',
    loadComponent: () =>
      import('./pages/cart/cart.component').then((m) => m.CartComponent),
  },
  {
    path: 'my',
    loadComponent: () =>
      import('./pages/order-list/order-list.component').then((m) => m.OrderListComponent),
  },
  {
    path: 'approvals',
    canActivate: [roleGuard('ROLE_ADMIN')],
    loadComponent: () =>
      import('./pages/order-approval/order-approval.component').then(
        (m) => m.OrderApprovalComponent
      ),
  },
  { path: '', redirectTo: 'my', pathMatch: 'full' },
];

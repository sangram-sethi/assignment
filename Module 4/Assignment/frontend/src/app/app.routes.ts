import { Routes } from '@angular/router';
import { authGuard } from './core/auth/auth.guard';

export const routes: Routes = [
  { path: '', redirectTo: 'products', pathMatch: 'full' },
  {
    path: 'login',
    loadComponent: () =>
      import('./core/auth/login/login.component').then((m) => m.LoginComponent),
  },
  {
    path: 'products',
    canActivate: [authGuard],
    loadChildren: () =>
      import('./features/products/products.routes').then((m) => m.PRODUCTS_ROUTES),
  },
  {
    path: 'orders',
    canActivate: [authGuard],
    loadChildren: () =>
      import('./features/orders/orders.routes').then((m) => m.ORDERS_ROUTES),
  },
  // Fallback: unknown URLs go back to the default route.
  { path: '**', redirectTo: 'products' },
];

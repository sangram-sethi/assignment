import { Routes } from '@angular/router';
import { roleGuard } from '../../core/auth/role.guard';

export const PRODUCTS_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./pages/product-list/product-list.component').then(
        (m) => m.ProductListComponent
      ),
  },
  {
    path: 'add',
    canActivate: [roleGuard('ROLE_ADMIN')],
    loadComponent: () =>
      import('./pages/product-form/product-form.component').then(
        (m) => m.ProductFormComponent
      ),
  },
  {
    path: ':id/edit',
    canActivate: [roleGuard('ROLE_ADMIN')],
    loadComponent: () =>
      import('./pages/product-form/product-form.component').then(
        (m) => m.ProductFormComponent
      ),
  }
];
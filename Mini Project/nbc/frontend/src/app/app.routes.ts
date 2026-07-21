import { inject } from '@angular/core';
import { Routes } from '@angular/router';
import { authGuard, guestGuard, roleGuard } from './core/guards/auth.guard';
import { AuthService } from './core/services/auth.service';

export const routes: Routes = [
  {
    path: 'login',
    canActivate: [guestGuard],
    loadComponent: () => import('./features/auth/login').then((m) => m.Login),
    title: 'Sign in · Aurora Loan Studio',
  },
  {
    path: 'register',
    canActivate: [guestGuard],
    loadComponent: () => import('./features/auth/register').then((m) => m.Register),
    title: 'Create account · Aurora Loan Studio',
  },
  {
    path: '',
    canActivate: [authGuard],
    loadComponent: () => import('./layout/shell').then((m) => m.Shell),
    children: [
      {
        path: '',
        pathMatch: 'full',
        redirectTo: () => inject(AuthService).homeRoute(),
      },
      {
        path: 'dashboard',
        canActivate: [roleGuard],
        data: { roles: ['CUSTOMER'] },
        loadComponent: () => import('./features/dashboard/dashboard').then((m) => m.Dashboard),
        title: 'Dashboard · Aurora',
      },
      {
        path: 'calculator',
        canActivate: [roleGuard],
        data: { roles: ['CUSTOMER'] },
        loadComponent: () => import('./features/calculator/calculator').then((m) => m.Calculator),
        title: 'EMI Calculator · Aurora',
      },
      {
        path: 'apply',
        canActivate: [roleGuard],
        data: { roles: ['CUSTOMER'] },
        loadComponent: () => import('./features/apply/apply').then((m) => m.Apply),
        title: 'Apply for a Loan · Aurora',
      },
      {
        path: 'loans',
        canActivate: [roleGuard],
        data: { roles: ['CUSTOMER'] },
        loadComponent: () => import('./features/loans/loans').then((m) => m.Loans),
        title: 'My Loans · Aurora',
      },
      {
        path: 'loans/:id',
        canActivate: [roleGuard],
        data: { roles: ['CUSTOMER'] },
        loadComponent: () => import('./features/loans/loan-detail').then((m) => m.LoanDetail),
        title: 'Loan Details · Aurora',
      },
      {
        path: 'approvals',
        canActivate: [roleGuard],
        data: { roles: ['LOAN_APPROVER'] },
        loadComponent: () => import('./features/approvals/approvals').then((m) => m.Approvals),
        title: 'Approval Queue · Aurora',
      },
      {
        path: 'profile',
        canActivate: [roleGuard],
        data: { roles: ['CUSTOMER', 'LOAN_APPROVER'] },
        loadComponent: () => import('./features/profile/profile').then((m) => m.Profile),
        title: 'Profile · Aurora',
      },
    ],
  },
  { path: '**', redirectTo: '' },
];

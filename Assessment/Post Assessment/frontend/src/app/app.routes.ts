import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: 'tracks',
    loadChildren: () =>
      import('./features/tracks/tracks.routes').then((m) => m.TRACKS_ROUTES),
  },
  { path: '', pathMatch: 'full', redirectTo: 'tracks' },
  { path: '**', redirectTo: 'tracks' },
];

import { Routes } from '@angular/router';

export const TRACKS_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./pages/track-list/track-list').then((m) => m.TrackList),
  },
];

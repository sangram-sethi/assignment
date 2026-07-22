import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../../environments/environment';
import { Track, TrackRequest } from '../models/track.model';

/**
 * Data-access service for the Track resource.
 * Maps 1:1 to the backend `music/platform/v1/tracks` endpoints.
 */
@Injectable({ providedIn: 'root' })
export class TrackService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/tracks`;

  getAll(): Observable<Track[]> {
    return this.http.get<Track[]>(this.baseUrl);
  }

  getByTitle(title: string): Observable<Track> {
    return this.http.get<Track>(`${this.baseUrl}/search`, {
      params: { title },
    });
  }

  create(track: TrackRequest): Observable<Track> {
    return this.http.post<Track>(this.baseUrl, track);
  }

  delete(trackId: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${trackId}`);
  }
}

import {
  ChangeDetectionStrategy,
  Component,
  computed,
  inject,
  signal,
} from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { Track, TrackRequest } from '../../models/track.model';
import { TrackService } from '../../services/track.service';
import { TrackForm } from '../../components/track-form/track-form';

const MAX_SUGGESTIONS = 8;

@Component({
  selector: 'app-track-list',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [FormsModule, DatePipe, TrackForm],
  templateUrl: './track-list.html',
})
export class TrackList {
  private readonly trackService = inject(TrackService);

  /** Full catalogue loaded once; all filtering is derived from this. */
  private readonly allTracks = signal<Track[]>([]);
  protected readonly loading = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly query = signal('');
  protected readonly showSuggestions = signal(false);

  /** Tracks shown in the table, prefix-filtered and ranked by play count. */
  protected readonly filteredTracks = computed(() =>
    this.rank(this.allTracks(), this.query()),
  );

  /** Autocomplete dropdown entries: top most-played prefix matches. */
  protected readonly suggestions = computed(() => {
    const term = this.query().trim();
    if (!term) {
      return [];
    }
    return this.rank(this.allTracks(), term).slice(0, MAX_SUGGESTIONS);
  });

  constructor() {
    this.loadTracks();
  }

  protected loadTracks(): void {
    this.loading.set(true);
    this.error.set(null);
    this.trackService.getAll().subscribe({
      next: (tracks) => {
        this.allTracks.set(tracks);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Unable to load tracks. Is the backend running?');
        this.loading.set(false);
      },
    });
  }

  protected onQueryChange(value: string): void {
    this.query.set(value);
    this.showSuggestions.set(value.trim().length > 0);
  }

  protected selectSuggestion(track: Track): void {
    this.query.set(track.title);
    this.showSuggestions.set(false);
  }

  protected clearSearch(): void {
    this.query.set('');
    this.showSuggestions.set(false);
  }

  protected createTrack(request: TrackRequest): void {
    this.error.set(null);
    this.trackService.create(request).subscribe({
      next: (created) => this.allTracks.update((list) => [created, ...list]),
      error: () => this.error.set('Failed to create the track.'),
    });
  }

  protected deleteTrack(id: number): void {
    this.error.set(null);
    this.trackService.delete(id).subscribe({
      next: () =>
        this.allTracks.update((list) => list.filter((t) => t.id !== id)),
      error: () => this.error.set('Failed to delete the track.'),
    });
  }

  /**
   * Prefix-match tracks by title or album name (case-insensitive) and order
   * them by play count (most played first). An empty term returns every track
   * ranked by popularity.
   */
  private rank(tracks: Track[], term: string): Track[] {
    const needle = term.trim().toLowerCase();
    const matches = needle
      ? tracks.filter(
          (t) =>
            t.title?.toLowerCase().startsWith(needle) ||
            t.albumName?.toLowerCase().startsWith(needle),
        )
      : [...tracks];
    return matches.sort((a, b) => (b.playCount ?? 0) - (a.playCount ?? 0));
  }
}

import {
  ChangeDetectionStrategy,
  Component,
  computed,
  inject,
  signal,
} from '@angular/core';
import { DatePipe, DecimalPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { Track, TrackRequest } from '../../models/track.model';
import { TrackService } from '../../services/track.service';
import { TrackForm } from '../../components/track-form/track-form';

const MAX_SUGGESTIONS = 8;

const ACCENTS = [
  'from-indigo-500 to-fuchsia-500',
  'from-sky-500 to-blue-600',
  'from-rose-500 to-orange-500',
  'from-emerald-500 to-teal-500',
  'from-violet-500 to-purple-600',
  'from-amber-500 to-pink-500',
];

@Component({
  selector: 'app-track-list',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [FormsModule, DatePipe, DecimalPipe, TrackForm],
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
  protected readonly showAddPanel = signal(false);

  /** Tracks shown in the table, prefix-filtered and ranked by play count. */
  protected readonly filteredTracks = computed(() =>
    this.rank(this.allTracks(), this.query()),
  );

  /** Aggregate stats for the hero header. */
  protected readonly totalTracks = computed(() => this.allTracks().length);
  protected readonly totalPlays = computed(() =>
    this.allTracks().reduce((sum, t) => sum + (t.playCount ?? 0), 0),
  );
  protected readonly totalAlbums = computed(
    () => new Set(this.allTracks().map((t) => t.albumName)).size,
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

  protected toggleAddPanel(): void {
    this.showAddPanel.update((open) => !open);
  }

  protected createTrack(request: TrackRequest): void {
    this.error.set(null);
    this.trackService.create(request).subscribe({
      next: (created) => {
        this.allTracks.update((list) => [created, ...list]);
        this.showAddPanel.set(false);
      },
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

  /** First letter used for the generated album-art tile. */
  protected initial(track: Track): string {
    return (track.title?.trim()[0] ?? '?').toUpperCase();
  }

  /** Deterministic gradient per track so art tiles look consistent. */
  protected accent(track: Track): string {
    const key = track.title ?? '';
    let hash = 0;
    for (let i = 0; i < key.length; i++) {
      hash = (hash * 31 + key.charCodeAt(i)) >>> 0;
    }
    return ACCENTS[hash % ACCENTS.length];
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

export interface Track {
  id: number;
  title: string;
  albumName: string;
  releaseDate: string;
  playCount: number;
}

export type TrackRequest = Omit<Track, 'id'>;

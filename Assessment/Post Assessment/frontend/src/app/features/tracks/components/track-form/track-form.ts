import { ChangeDetectionStrategy, Component, output } from '@angular/core';
import {
  FormBuilder,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';

import { TrackRequest } from '../../models/track.model';

@Component({
  selector: 'app-track-form',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [ReactiveFormsModule],
  templateUrl: './track-form.html',
})
export class TrackForm {
  private readonly fb = new FormBuilder();

  readonly submitted = output<TrackRequest>();

  protected readonly form = this.fb.nonNullable.group({
    title: ['', [Validators.required, Validators.maxLength(120)]],
    albumName: ['', [Validators.required, Validators.maxLength(120)]],
    releaseDate: ['', [Validators.required]],
    playCount: [0, [Validators.required, Validators.min(0)]],
  });

  protected onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.submitted.emit(this.form.getRawValue());
    this.form.reset({ title: '', albumName: '', releaseDate: '', playCount: 0 });
  }
}

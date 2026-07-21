import { ChangeDetectionStrategy, Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { AuroraBackground } from './shared/components/aurora-bg';
import { Toasts } from './shared/components/toasts';

@Component({
  selector: 'app-root',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterOutlet, AuroraBackground, Toasts],
  templateUrl: './app.html',
  styleUrl: './app.scss',
})
export class App {}

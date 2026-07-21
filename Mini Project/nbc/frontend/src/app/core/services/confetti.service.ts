import { Injectable } from '@angular/core';
import confetti from 'canvas-confetti';

/** Celebratory micro-interactions for approvals and successful submissions. */
@Injectable({ providedIn: 'root' })
export class ConfettiService {
  private readonly colors = ['#7c5cff', '#b06bff', '#ff6ea9', '#38bdf8', '#34d399', '#fbbf24'];

  burst(origin: { x?: number; y?: number } = {}): void {
    confetti({
      particleCount: 120,
      spread: 78,
      startVelocity: 42,
      scalar: 0.95,
      ticks: 220,
      origin: { x: origin.x ?? 0.5, y: origin.y ?? 0.55 },
      colors: this.colors,
      disableForReducedMotion: true,
    });
  }

  /** Two angled cannons for a richer celebration. */
  cannons(): void {
    const fire = (angle: number, x: number) =>
      confetti({
        particleCount: 90,
        angle,
        spread: 62,
        startVelocity: 55,
        origin: { x, y: 0.7 },
        colors: this.colors,
        disableForReducedMotion: true,
      });
    fire(60, 0.08);
    fire(120, 0.92);
  }

  /** Fire from a specific screen coordinate (e.g. a button press). */
  fromElement(el: Element): void {
    const rect = el.getBoundingClientRect();
    this.burst({
      x: (rect.left + rect.width / 2) / window.innerWidth,
      y: (rect.top + rect.height / 2) / window.innerHeight,
    });
  }
}

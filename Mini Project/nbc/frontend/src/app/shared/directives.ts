import {
  Directive,
  ElementRef,
  OnDestroy,
  OnInit,
  effect,
  inject,
  input,
} from '@angular/core';
import gsap from 'gsap';
import { formatCompactINR, formatINR, formatNumber } from './format';

const prefersReduced = () =>
  typeof matchMedia !== 'undefined' && matchMedia('(prefers-reduced-motion: reduce)').matches;

/* ------------------------------------------------------------------ count-up */
type CountFormat = 'inr' | 'inr2' | 'compact' | 'number' | 'plain';

@Directive({ selector: '[countUp]', standalone: true })
export class CountUp implements OnDestroy {
  private readonly host = inject(ElementRef<HTMLElement>).nativeElement;

  readonly countUp = input.required<number>();
  readonly countFormat = input<CountFormat>('number');
  readonly countDuration = input<number>(1.4);
  readonly countPrefix = input<string>('');
  readonly countSuffix = input<string>('');

  private entered = false;
  private current = 0;
  private observer?: IntersectionObserver;
  private tween?: gsap.core.Tween;

  constructor() {
    this.observer = new IntersectionObserver(
      (entries) => {
        if (entries.some((e) => e.isIntersecting) && !this.entered) {
          this.entered = true;
          this.animateTo(this.countUp());
        }
      },
      { threshold: 0.25 },
    );
    // defer until element is in DOM
    queueMicrotask(() => this.observer?.observe(this.host));

    effect(() => {
      const target = this.countUp();
      if (this.entered) this.animateTo(target);
      else this.render(0);
    });
  }

  private animateTo(target: number): void {
    if (prefersReduced()) {
      this.current = target;
      this.render(target);
      return;
    }
    this.tween?.kill();
    const state = { v: this.current };
    this.tween = gsap.to(state, {
      v: target,
      duration: this.countDuration(),
      ease: 'power2.out',
      onUpdate: () => {
        this.current = state.v;
        this.render(state.v);
      },
    });
  }

  private render(v: number): void {
    let body: string;
    switch (this.countFormat()) {
      case 'inr':
        body = formatINR(v);
        break;
      case 'inr2':
        body = formatINR(v, true);
        break;
      case 'compact':
        body = formatCompactINR(v);
        break;
      case 'number':
        body = formatNumber(v);
        break;
      default:
        body = String(Math.round(v));
    }
    this.host.textContent = `${this.countPrefix()}${body}${this.countSuffix()}`;
  }

  ngOnDestroy(): void {
    this.tween?.kill();
    this.observer?.disconnect();
  }
}

/* -------------------------------------------------------------------- reveal */
type RevealDir = 'up' | 'down' | 'left' | 'right' | 'scale';

@Directive({ selector: '[reveal]', standalone: true })
export class Reveal implements OnInit, OnDestroy {
  private readonly host = inject(ElementRef<HTMLElement>).nativeElement;

  readonly reveal = input<RevealDir | ''>('up');
  readonly revealDelay = input<number>(0);
  readonly revealDistance = input<number>(22);
  readonly revealDuration = input<number>(0.8);

  private observer?: IntersectionObserver;

  ngOnInit(): void {
    const dir = this.reveal() || 'up';
    if (prefersReduced()) {
      gsap.set(this.host, { opacity: 1, x: 0, y: 0, scale: 1 });
      return;
    }
    const d = this.revealDistance();
    const from: gsap.TweenVars = { opacity: 0 };
    if (dir === 'up') from['y'] = d;
    else if (dir === 'down') from['y'] = -d;
    else if (dir === 'left') from['x'] = d;
    else if (dir === 'right') from['x'] = -d;
    else if (dir === 'scale') from['scale'] = 0.94;
    gsap.set(this.host, from);

    this.observer = new IntersectionObserver(
      (entries, obs) => {
        for (const e of entries) {
          if (e.isIntersecting) {
            gsap.to(this.host, {
              opacity: 1,
              x: 0,
              y: 0,
              scale: 1,
              duration: this.revealDuration(),
              delay: this.revealDelay(),
              ease: 'power3.out',
            });
            obs.unobserve(e.target);
          }
        }
      },
      { threshold: 0.12, rootMargin: '0px 0px -8% 0px' },
    );
    queueMicrotask(() => this.observer?.observe(this.host));
  }

  ngOnDestroy(): void {
    this.observer?.disconnect();
  }
}

/* ------------------------------------------------------------------ magnetic */
@Directive({
  selector: '[magnetic]',
  standalone: true,
  host: {
    '(pointermove)': 'onMove($event)',
    '(pointerleave)': 'onLeave()',
  },
})
export class Magnetic {
  private readonly host = inject(ElementRef<HTMLElement>).nativeElement;
  readonly magneticStrength = input<number>(0.35);

  private xTo = gsap.quickTo(this.host, 'x', { duration: 0.5, ease: 'power3.out' });
  private yTo = gsap.quickTo(this.host, 'y', { duration: 0.5, ease: 'power3.out' });

  onMove(e: PointerEvent): void {
    if (prefersReduced()) return;
    const r = this.host.getBoundingClientRect();
    const s = this.magneticStrength();
    this.xTo((e.clientX - (r.left + r.width / 2)) * s);
    this.yTo((e.clientY - (r.top + r.height / 2)) * s);
  }

  onLeave(): void {
    this.xTo(0);
    this.yTo(0);
  }
}

/* ---------------------------------------------------------------------- tilt */
@Directive({
  selector: '[tilt]',
  standalone: true,
  host: {
    '(pointermove)': 'onMove($event)',
    '(pointerleave)': 'onLeave()',
    style: 'transform-style: preserve-3d;',
  },
})
export class Tilt {
  private readonly host = inject(ElementRef<HTMLElement>).nativeElement;
  readonly tiltMax = input<number>(8);

  private rxTo = gsap.quickTo(this.host, 'rotationX', { duration: 0.5, ease: 'power3.out' });
  private ryTo = gsap.quickTo(this.host, 'rotationY', { duration: 0.5, ease: 'power3.out' });

  onMove(e: PointerEvent): void {
    if (prefersReduced()) return;
    const r = this.host.getBoundingClientRect();
    const px = (e.clientX - r.left) / r.width - 0.5;
    const py = (e.clientY - r.top) / r.height - 0.5;
    const max = this.tiltMax();
    this.ryTo(px * max * 2);
    this.rxTo(-py * max * 2);
  }

  onLeave(): void {
    this.rxTo(0);
    this.ryTo(0);
  }
}

/* -------------------------------------------------------------------- ripple */
@Directive({
  selector: '[ripple]',
  standalone: true,
  host: {
    '(pointerdown)': 'spawn($event)',
    style: 'position: relative; overflow: hidden;',
  },
})
export class Ripple {
  private readonly host = inject(ElementRef<HTMLElement>).nativeElement;

  spawn(e: PointerEvent): void {
    if (prefersReduced()) return;
    const r = this.host.getBoundingClientRect();
    const size = Math.max(r.width, r.height);
    const span = document.createElement('span');
    span.style.cssText = `position:absolute;left:${e.clientX - r.left - size / 2}px;top:${
      e.clientY - r.top - size / 2
    }px;width:${size}px;height:${size}px;border-radius:50%;background:radial-gradient(circle,rgba(255,255,255,.35),transparent 70%);pointer-events:none;transform:scale(0);opacity:.8;z-index:0;`;
    this.host.appendChild(span);
    gsap.to(span, {
      scale: 1,
      opacity: 0,
      duration: 0.6,
      ease: 'power2.out',
      onComplete: () => span.remove(),
    });
  }
}

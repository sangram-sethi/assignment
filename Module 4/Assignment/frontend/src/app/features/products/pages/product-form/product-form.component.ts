import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';

@Component({
  selector: 'app-product-form',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './product-form.component.html',
})
export class ProductFormComponent implements OnInit {
    private readonly route = inject(ActivatedRoute);
    protected readonly router = inject(Router);

    readonly productId = signal<number | null>(null);
    readonly isEditMode = computed(() => this.productId() !== null);

    ngOnInit(): void {
        const idParam = this.route.snapshot.paramMap.get('id');
        this.productId.set(idParam ? Number(idParam) : null);
    }
}
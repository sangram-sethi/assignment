import { Component, inject, OnInit, signal } from "@angular/core";
import { ProductService } from "../../services/product.service";
import { Product, Category } from "../../models/product.model";
import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { toUserMessage } from '../../../../core/http/http-error.util';
import { Router } from "@angular/router";
import { AuthService } from "../../../../core/auth/auth.service";
import { CartService } from "../../../orders/services/cart.service";

@Component({
  selector: 'app-product-list',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './product-list.component.html',
})
export class ProductListComponent implements OnInit {
    private readonly productService = inject(ProductService);
    private readonly router = inject(Router);
    private readonly auth = inject(AuthService);
    private readonly cart = inject(CartService);
    readonly isAdmin = this.auth.isAdmin;

    readonly products = signal<Product[]>([]);
    readonly loading = signal(false);
    readonly error = signal<string | null>(null);
    /** Name of the product most recently added to the cart, for a brief confirmation. */
    readonly addedName = signal<string | null>(null);

    ngOnInit(): void {
        this.loadProducts();
    }

    loadProducts(): void {
        this.loading.set(true);
        this.error.set(null);

        this.productService.getProducts().subscribe({
            next: (data) => {
                this.products.set(data);
                this.loading.set(false);
            },
            error: (err: HttpErrorResponse) => {
                this.error.set(toUserMessage(err));
                this.loading.set(false);
                console.error(err);
            }
        });
    }

    addProduct(): void {
        this.router.navigate(['/products/add']);
    }

    addToCart(product: Product): void {
        this.cart.add(product);
        this.addedName.set(product.productName);
        setTimeout(() => this.addedName.set(null), 2000);
    }

    /** Emoji thumbnail used on the product card for each category. */
    categoryIcon(category: Category): string {
        switch (category) {
            case 'Laptop': return '💻';
            case 'Mobile': return '📱';
            case 'Tablet': return '🖥️';
            default: return '🛍️';
        }
    }

    editProduct(id: number): void {
        this.router.navigate(['/products', id, 'edit']);
    }

    deleteProduct(id: number): void {
        if (!confirm('Delete this product? This action cannot be undone.')) {
            return;
        }
        this.productService.deleteProduct(id).subscribe({
            next: () => this.loadProducts(),
            error: (err) => this.error.set(toUserMessage(err))
        });   
    }
}
import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { Product, ProductRequest, Category } from '../models/product.model';


@Injectable({ providedIn: 'root' })
export class ProductService {
    private readonly http = inject(HttpClient);
    private readonly baseUrl = `${environment.apiUrl}/products`;


    // GET /api/products with optional filters, sorting, paging
  getProducts(options?: {
    category?: Category;
    brand?: string;
    sortBy?: string;
    direction?: 'asc' | 'desc';
    page?: number;
    size?: number;
  }): Observable<Product[]> {
    let params = new HttpParams();
    if (options?.category) params = params.set('category', options.category);
    if (options?.brand) params = params.set('brand', options.brand);
    if (options?.sortBy) params = params.set('sortBy', options.sortBy);
    if (options?.direction) params = params.set('direction', options.direction);
    if (options?.page != null) params = params.set('page', options.page);
    if (options?.size != null) params = params.set('size', options.size);

    return this.http.get<Product[]>(this.baseUrl, { params });
  }

  // GET /api/products/{id}
  getProductById(id: number): Observable<Product> {
    return this.http.get<Product>(`${this.baseUrl}/${id}`);
  }

  // POST /api/products  (ADMIN only)
  createProduct(product: ProductRequest): Observable<Product> {
    return this.http.post<Product>(this.baseUrl, product);
  }

  // PUT /api/products/{id}  (ADMIN only)
  updateProduct(id: number, product: ProductRequest): Observable<Product> {
    return this.http.put<Product>(`${this.baseUrl}/${id}`, product);
  }

  // DELETE /api/products/{id}  (ADMIN only)
  deleteProduct(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
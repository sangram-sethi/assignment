export type Category = 'Laptop' | 'Mobile' | 'Tablet';

export interface Product {
    id: number;
    category: Category;
    productName: string;
    brand: string;
    price: number;
}

export interface ProductRequest {
    category: Category;
    productName: string;
    brand: string;
    price: number;
}


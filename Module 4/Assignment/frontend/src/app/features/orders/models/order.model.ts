export type OrderStatus = 'PENDING' | 'APPROVED' | 'REJECTED';

export interface OrderItem {
    productId: number;
    productName: string;
    quantity: number;
    unitPrice: number;
    subtotal: number;
}

export interface Order {
    orderId: number;
    customerId: number;
    status: OrderStatus;
    items: OrderItem[];
    totalAmount: number;
}

export interface OrderItemRequest {
    productId: number;
    quantity: number;
}

// The customer is derived from the authenticated user on the server, so the
// client only sends the items.
export interface OrderRequest {
    orderItems: OrderItemRequest[];
}
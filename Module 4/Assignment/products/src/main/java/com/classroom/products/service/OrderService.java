package com.classroom.products.service;

import com.classroom.products.dto.OrderRequestDTO;
import com.classroom.products.dto.OrderResponseDTO;

import java.util.List;

public interface OrderService {
    
    OrderResponseDTO placeOrder(OrderRequestDTO request);

    OrderResponseDTO getOrderById(Long orderId);

    List<OrderResponseDTO> getAllOrders();

    List<OrderResponseDTO> getOrdersByCustomer(Long customerId);

    OrderResponseDTO approveOrder(Long orderId);

    OrderResponseDTO rejectOrder(Long orderId);

    void cancelOrder(Long orderId);
    
}

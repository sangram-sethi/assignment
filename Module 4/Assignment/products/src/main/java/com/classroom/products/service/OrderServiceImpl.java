package com.classroom.products.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.classroom.products.dto.OrderItemResponseDTO;
import com.classroom.products.dto.OrderRequestDTO;
import com.classroom.products.dto.OrderResponseDTO;
import com.classroom.products.enums.OrderStatus;
import com.classroom.products.exception.BadRequestException;
import com.classroom.products.exception.ResourceNotFoundException;
import com.classroom.products.model.OrderItem;
import com.classroom.products.model.Orders;
import com.classroom.products.model.Product;
import com.classroom.products.repository.CustomerRepository;
import com.classroom.products.repository.OrdersRepository;
import com.classroom.products.repository.ProductRepository;

@Service
public class OrderServiceImpl implements OrderService {

    private final ProductRepository productRepository;
    private final OrdersRepository orderRepository;
    private final CustomerRepository customerRepository;

    public OrderServiceImpl(ProductRepository productRepository, OrdersRepository orderRepository,
            CustomerRepository customerRepository) {
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
    }

    private Orders findOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Order not found with id: " + orderId));
    }

    
    private OrderItemResponseDTO mapToOrderItemResponse(OrderItem item) {
        return new OrderItemResponseDTO(
                item.getProduct().getId(),
                item.getProduct().getProductName(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getQuantity() * item.getUnitPrice());
    }

    private OrderResponseDTO mapToOrderResponse(Orders order) {
        List<OrderItemResponseDTO> items = order.getOrderItems().stream()
                .map(this::mapToOrderItemResponse)
                .toList();
        Double totalAmount = items.stream()
                .mapToDouble(OrderItemResponseDTO::getSubtotal)
                .sum();
        return new OrderResponseDTO(
                order.getOrderId(),
                order.getCustomer().getCustomerId(),
                order.getStatus() != null ? order.getStatus().name() : null,
                items,
                totalAmount);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public OrderResponseDTO approveOrder(Long orderId) {
        Orders order = findOrderById(orderId);
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BadRequestException(
                    "Only PENDING orders can be approved. Current status: " + order.getStatus());
        }
        order.setStatus(OrderStatus.APPROVED);
        return mapToOrderResponse(orderRepository.save(order));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public OrderResponseDTO rejectOrder(Long orderId) {
        Orders order = findOrderById(orderId);
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BadRequestException(
                    "Only PENDING orders can be rejected. Current status: " + order.getStatus());
        }
        order.setStatus(OrderStatus.REJECTED);
        return mapToOrderResponse(orderRepository.save(order));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public void cancelOrder(Long orderId) {
        orderRepository.delete(findOrderById(orderId));
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public List<OrderResponseDTO> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(this::mapToOrderResponse)
                .toList();
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public OrderResponseDTO getOrderById(Long orderId) {
        return mapToOrderResponse(findOrderById(orderId));
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public List<OrderResponseDTO> getOrdersByCustomer(Long customerId) {
        return orderRepository.findByCustomerCustomerId(customerId).stream()
                .map(this::mapToOrderResponse)
                .toList();
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public OrderResponseDTO placeOrder(OrderRequestDTO request) {
        Orders order = new Orders();
        order.setStatus(OrderStatus.PENDING);
        order.setCustomer(customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Customer not found with id: " + request.getCustomerId()
                )));

        order.setOrderItems(request.getOrderItems().stream()
                .map(itemRequest -> {
                    Product product = productRepository.findById(itemRequest.getProductId())
                            .orElseThrow(() -> new ResourceNotFoundException(
                                "Product not found with id: " + itemRequest.getProductId()
                            ));
                    OrderItem orderItem = new OrderItem();
                    orderItem.setProduct(product);
                    orderItem.setQuantity(itemRequest.getQuantity());
                    orderItem.setUnitPrice(product.getPrice());
                    orderItem.setOrder(order);
                    return orderItem;
                })
                .collect(Collectors.toList()));

        Orders savedOrder = orderRepository.save(order);
        return mapToOrderResponse(savedOrder);
    }

}

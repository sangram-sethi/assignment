package com.classroom.products.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponseDTO {
    
    private Long orderId;

    private Long customerId;

    private String status;

    private List<OrderItemResponseDTO> items;

    private Double totalAmount;

}

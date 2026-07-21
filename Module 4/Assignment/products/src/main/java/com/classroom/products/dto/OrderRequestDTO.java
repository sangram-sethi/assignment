package com.classroom.products.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderRequestDTO {

    // Optional and ignored by the server: the customer is derived from the
    // authenticated user. Retained only for backward compatibility.
    private Long customerId;

    @NotEmpty(message = "Order must contain at least one item")
    @Valid
    private List<OrderItemRequestDTO> orderItems;

}

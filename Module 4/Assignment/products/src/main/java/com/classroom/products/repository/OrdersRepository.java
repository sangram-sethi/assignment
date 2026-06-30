package com.classroom.products.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.classroom.products.model.Orders;

public interface OrdersRepository extends JpaRepository<Orders, Long> {

    List<Orders> findByCustomerCustomerId(Long customerId);
}

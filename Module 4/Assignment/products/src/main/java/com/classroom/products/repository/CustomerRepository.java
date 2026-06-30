package com.classroom.products.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.classroom.products.model.Customer;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    
}

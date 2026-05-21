package com.example.customer_management.controller;

import com.example.customer_management.dto.CustomerRequest;
import com.example.customer_management.entity.Customer;
import com.example.customer_management.service.CustomerService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/customers")
@SecurityRequirement(name = "bearerAuth")
public class CustomerController {

    @Autowired
    private CustomerService service;


    @GetMapping
    public ResponseEntity<Page<Customer>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {

        if (keyword != null && !keyword.isBlank()) {
            return ResponseEntity.ok(service.search(keyword, page, size));
        }

        return ResponseEntity.ok(service.getAll(page, size));
    }


    @GetMapping("/{id}")
    public ResponseEntity<Customer> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(service.getById(id));
    }


    @PostMapping
    public ResponseEntity<Customer> create(
            @Valid @RequestBody CustomerRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.create(req));
    }


    @PutMapping("/{id}")
    public ResponseEntity<Customer> update(
            @PathVariable Integer id,
            @Valid @RequestBody CustomerRequest req) {
        return ResponseEntity.ok(service.update(id, req));
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}

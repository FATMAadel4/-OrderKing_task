package com.example.customer_management.service;
import com.example.customer_management.dto.CustomerRequest;

import com.example.customer_management.entity.Customer;
import com.example.customer_management.exception.ConflictException;
import com.example.customer_management.exception.NotFoundException;
import com.example.customer_management.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

@Service
public class CustomerService {

    @Autowired
    private CustomerRepository repo;


    public Page<Customer> getAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size,
                Sort.by("createdAt").descending());
        return repo.findAll(pageable);
    }


    public Page<Customer> search(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size,
                Sort.by("createdAt").descending());
        return repo.search(keyword, pageable);
    }


    public Customer getById(Integer id) {
        return repo.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        "Customer not found with id: " + id));
    }


    public Customer create(CustomerRequest req) {


        if (repo.existsByEmail(req.getEmail())) {
            throw new ConflictException(
                    "Email already exists: " + req.getEmail());
        }

        Customer customer = Customer.builder()
                .name(req.getName())
                .email(req.getEmail())
                .phone(req.getPhone())
                .build();

        return repo.save(customer);
    }


    public Customer update(Integer id, CustomerRequest req) {

        Customer customer = getById(id);

        if (repo.existsByEmailAndIdNot(req.getEmail(), id)) {
            throw new ConflictException(
                    "Email already exists: " + req.getEmail());
        }

        customer.setName(req.getName());
        customer.setEmail(req.getEmail());
        customer.setPhone(req.getPhone());

        return repo.save(customer);
    }


    public void delete(Integer id) {
        Customer customer = getById(id);
        repo.delete(customer);
    }
}

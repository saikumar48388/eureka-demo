package com.example.customerservice.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Service;

import com.example.customerservice.model.Customer;

@Service
public class CustomerService {

    private final Map<Long, Customer> store = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(0);

    public CustomerService() {
        add(new Customer(null, "Sai Kumar", "saikumar@example.com", "9876543210", "Benz Circle, Vijayawada"));
        add(new Customer(null, "Anitha Reddy", "anitha@example.com", "9123456780", "Madhapur, Hyderabad"));
    }

    public List<Customer> getAll() {
        List<Customer> list = new ArrayList<>(store.values());
        list.sort((a, b) -> a.getId().compareTo(b.getId()));
        return list;
    }

    public Optional<Customer> getById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    public Customer add(Customer customer) {
        long id = idGenerator.incrementAndGet();
        customer.setId(id);
        store.put(id, customer);
        return customer;
    }

    public Optional<Customer> update(Long id, Customer updated) {
        if (!store.containsKey(id)) {
            return Optional.empty();
        }
        updated.setId(id);
        store.put(id, updated);
        return Optional.of(updated);
    }

    public boolean delete(Long id) {
        return store.remove(id) != null;
    }
}

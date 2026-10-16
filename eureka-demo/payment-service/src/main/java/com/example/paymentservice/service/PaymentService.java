package com.example.paymentservice.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Service;

import com.example.paymentservice.model.Payment;

@Service
public class PaymentService {

    private final Map<Long, Payment> store = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(0);

    /** Simulated payment: succeeds when amount > 0 and a method is given. */
    public Payment process(Payment payment) {
        payment.setId(idGenerator.incrementAndGet());
        payment.setCreatedAt(LocalDateTime.now().toString());
        boolean valid = payment.getAmount() > 0 && payment.getMethod() != null && !payment.getMethod().isBlank();
        payment.setStatus(valid ? "SUCCESS" : "FAILED");
        store.put(payment.getId(), payment);
        return payment;
    }

    public List<Payment> getAll() {
        List<Payment> list = new ArrayList<>(store.values());
        list.sort((a, b) -> a.getId().compareTo(b.getId()));
        return list;
    }

    public Optional<Payment> getById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    public List<Payment> getByOrder(Long orderId) {
        return getAll().stream().filter(p -> orderId.equals(p.getOrderId())).toList();
    }
}

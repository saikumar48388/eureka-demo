package com.example.deliveryservice.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Service;

import com.example.deliveryservice.model.Delivery;

@Service
public class DeliveryService {

    private static final List<String> DRIVERS = List.of("Ravi", "Suresh", "Anil", "Kiran");
    public static final Set<String> VALID_STATUS = Set.of("ASSIGNED", "PICKED_UP", "OUT_FOR_DELIVERY", "DELIVERED");

    private final Map<Long, Delivery> store = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(0);
    private final AtomicInteger driverCounter = new AtomicInteger(0);

    /** Creates a delivery and assigns the next driver (round robin). */
    public Delivery assign(Delivery delivery) {
        delivery.setId(idGenerator.incrementAndGet());
        delivery.setDriverName(DRIVERS.get(driverCounter.getAndIncrement() % DRIVERS.size()));
        delivery.setStatus("ASSIGNED");
        store.put(delivery.getId(), delivery);
        return delivery;
    }

    public List<Delivery> getAll() {
        List<Delivery> list = new ArrayList<>(store.values());
        list.sort((a, b) -> a.getId().compareTo(b.getId()));
        return list;
    }

    public Optional<Delivery> getById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    public List<Delivery> getByOrder(Long orderId) {
        return getAll().stream().filter(d -> orderId.equals(d.getOrderId())).toList();
    }

    public Optional<Delivery> updateStatus(Long id, String status) {
        Delivery delivery = store.get(id);
        if (delivery == null) {
            return Optional.empty();
        }
        delivery.setStatus(status);
        return Optional.of(delivery);
    }
}

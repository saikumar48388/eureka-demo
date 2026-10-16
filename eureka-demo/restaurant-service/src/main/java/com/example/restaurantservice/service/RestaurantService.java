package com.example.restaurantservice.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Service;

import com.example.restaurantservice.model.Restaurant;

@Service
public class RestaurantService {

    // simple in-memory store (no database needed for this lab)
    private final Map<Long, Restaurant> store = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(0);

    public RestaurantService() {
        add(new Restaurant(null, "Spice Hub", "Indian", "Vijayawada"));
        add(new Restaurant(null, "Pizza Palace", "Italian", "Hyderabad"));
        add(new Restaurant(null, "Biryani House", "Hyderabadi", "Hyderabad"));
    }

    public List<Restaurant> getAll() {
        List<Restaurant> list = new ArrayList<>(store.values());
        list.sort((a, b) -> a.getId().compareTo(b.getId()));
        return list;
    }

    public Optional<Restaurant> getById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    public Restaurant add(Restaurant restaurant) {
        long id = idGenerator.incrementAndGet();
        restaurant.setId(id);
        store.put(id, restaurant);
        return restaurant;
    }

    public Optional<Restaurant> update(Long id, Restaurant updated) {
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

package com.example.menuservice.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Service;

import com.example.menuservice.model.MenuItem;

@Service
public class MenuService {

    private final Map<Long, MenuItem> store = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(0);

    public MenuService() {
        // restaurantId matches the sample restaurants (1 = Spice Hub, 2 = Pizza Palace, 3 = Biryani House)
        add(new MenuItem(null, 1L, "Paneer Butter Masala", "Main Course", 220));
        add(new MenuItem(null, 1L, "Veg Thali", "Meals", 180));
        add(new MenuItem(null, 2L, "Margherita Pizza", "Pizza", 299));
        add(new MenuItem(null, 2L, "Garlic Bread", "Starter", 129));
        add(new MenuItem(null, 3L, "Chicken Biryani", "Biryani", 250));
        add(new MenuItem(null, 3L, "Mutton Biryani", "Biryani", 320));
    }

    public List<MenuItem> getAll() {
        List<MenuItem> list = new ArrayList<>(store.values());
        list.sort((a, b) -> a.getId().compareTo(b.getId()));
        return list;
    }

    public Optional<MenuItem> getById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    public List<MenuItem> getByRestaurant(Long restaurantId) {
        return getAll().stream().filter(m -> restaurantId.equals(m.getRestaurantId())).toList();
    }

    public MenuItem add(MenuItem item) {
        long id = idGenerator.incrementAndGet();
        item.setId(id);
        store.put(id, item);
        return item;
    }

    public Optional<MenuItem> update(Long id, MenuItem updated) {
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

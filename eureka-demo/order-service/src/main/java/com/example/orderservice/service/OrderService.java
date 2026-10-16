package com.example.orderservice.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.example.orderservice.dto.OrderRequest;
import com.example.orderservice.dto.RemoteDtos.Customer;
import com.example.orderservice.dto.RemoteDtos.Delivery;
import com.example.orderservice.dto.RemoteDtos.MenuItem;
import com.example.orderservice.dto.RemoteDtos.Payment;
import com.example.orderservice.dto.RemoteDtos.Restaurant;
import com.example.orderservice.exception.OrderException;
import com.example.orderservice.model.Order;
import com.example.orderservice.model.OrderLine;

@Service
public class OrderService {

    // Service names registered in Eureka - no host or port needed
    private static final String CUSTOMER_SERVICE = "http://customer-service";
    private static final String RESTAURANT_SERVICE = "http://restaurant-service";
    private static final String MENU_SERVICE = "http://menu-service";
    private static final String PAYMENT_SERVICE = "http://payment-service";
    private static final String DELIVERY_SERVICE = "http://delivery-service";

    private final RestTemplate restTemplate;
    private final Map<Long, Order> store = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(0);

    public OrderService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public Order placeOrder(OrderRequest request) {
        if (request.customerId() == null || request.restaurantId() == null
                || request.items() == null || request.items().isEmpty()) {
            throw new OrderException(HttpStatus.BAD_REQUEST,
                    "customerId, restaurantId and at least one item are required");
        }

        // 1. validate customer and restaurant
        Customer customer = fetch(CUSTOMER_SERVICE + "/customers/" + request.customerId(), Customer.class, "Customer");
        Restaurant restaurant = fetch(RESTAURANT_SERVICE + "/restaurants/" + request.restaurantId(), Restaurant.class, "Restaurant");

        // 2. price every item from the menu service
        List<OrderLine> lines = new ArrayList<>();
        double total = 0;
        for (OrderRequest.Item item : request.items()) {
            if (item.menuItemId() == null || item.quantity() <= 0) {
                throw new OrderException(HttpStatus.BAD_REQUEST, "Each item needs menuItemId and quantity > 0");
            }
            MenuItem menuItem = fetch(MENU_SERVICE + "/menu/" + item.menuItemId(), MenuItem.class,
                    "Menu item " + item.menuItemId());
            if (!restaurant.id().equals(menuItem.restaurantId())) {
                throw new OrderException(HttpStatus.BAD_REQUEST,
                        "Menu item " + menuItem.id() + " does not belong to restaurant " + restaurant.id());
            }
            lines.add(new OrderLine(menuItem.id(), menuItem.name(), item.quantity(), menuItem.price()));
            total += menuItem.price() * item.quantity();
        }

        // 3. save the order
        Order order = new Order();
        order.setId(idGenerator.incrementAndGet());
        order.setCustomerId(customer.id());
        order.setCustomerName(customer.name());
        order.setRestaurantId(restaurant.id());
        order.setRestaurantName(restaurant.name());
        order.setItems(lines);
        order.setTotalAmount(total);
        order.setDeliveryAddress(customer.address());
        order.setStatus("CREATED");
        order.setCreatedAt(LocalDateTime.now().toString());
        store.put(order.getId(), order);

        // 4. payment
        String method = request.paymentMethod() == null ? "COD" : request.paymentMethod();
        Payment payment;
        try {
            payment = restTemplate.postForObject(PAYMENT_SERVICE + "/payments",
                    Map.of("orderId", order.getId(), "amount", total, "method", method), Payment.class);
        } catch (RestClientException e) {
            throw new OrderException(HttpStatus.SERVICE_UNAVAILABLE, "Payment service is unavailable");
        }
        if (payment == null || !"SUCCESS".equals(payment.status())) {
            order.setStatus("PAYMENT_FAILED");
            return order;
        }
        order.setPaymentId(payment.id());

        // 5. delivery
        try {
            Delivery delivery = restTemplate.postForObject(DELIVERY_SERVICE + "/deliveries",
                    Map.of("orderId", order.getId(), "address", customer.address()), Delivery.class);
            order.setDeliveryId(delivery.id());
            order.setDriverName(delivery.driverName());
            order.setStatus("CONFIRMED");
        } catch (RestClientException e) {
            order.setStatus("PAID_DELIVERY_PENDING");   // paid, driver will be assigned later
        }
        return order;
    }

    public List<Order> getAll() {
        List<Order> list = new ArrayList<>(store.values());
        list.sort((a, b) -> a.getId().compareTo(b.getId()));
        return list;
    }

    public Optional<Order> getById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    public List<Order> getByCustomer(Long customerId) {
        return getAll().stream().filter(o -> customerId.equals(o.getCustomerId())).toList();
    }

    public Order cancel(Long id) {
        Order order = store.get(id);
        if (order == null) {
            throw new OrderException(HttpStatus.NOT_FOUND, "Order " + id + " not found");
        }
        order.setStatus("CANCELLED");
        return order;
    }

    private <T> T fetch(String url, Class<T> type, String what) {
        try {
            return restTemplate.getForObject(url, type);
        } catch (HttpClientErrorException.NotFound e) {
            throw new OrderException(HttpStatus.NOT_FOUND, what + " not found");
        } catch (RestClientException e) {
            throw new OrderException(HttpStatus.SERVICE_UNAVAILABLE, what + " lookup failed - service unavailable");
        }
    }
}

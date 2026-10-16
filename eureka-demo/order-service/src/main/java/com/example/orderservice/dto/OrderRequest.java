package com.example.orderservice.dto;

import java.util.List;

public record OrderRequest(Long customerId, Long restaurantId, List<Item> items, String paymentMethod) {

    public record Item(Long menuItemId, int quantity) {
    }
}

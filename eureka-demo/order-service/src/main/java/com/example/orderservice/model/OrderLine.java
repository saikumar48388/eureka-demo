package com.example.orderservice.model;

public record OrderLine(Long menuItemId, String name, int quantity, double unitPrice) {
}

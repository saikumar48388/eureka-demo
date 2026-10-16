package com.example.orderservice.dto;

/** Only the fields order-service needs from the responses of the other services. */
public final class RemoteDtos {

    private RemoteDtos() {
    }

    public record Customer(Long id, String name, String address) {
    }

    public record Restaurant(Long id, String name) {
    }

    public record MenuItem(Long id, Long restaurantId, String name, double price) {
    }

    public record Payment(Long id, String status) {
    }

    public record Delivery(Long id, String driverName, String status) {
    }
}

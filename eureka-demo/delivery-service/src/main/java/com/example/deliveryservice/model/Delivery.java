package com.example.deliveryservice.model;

public class Delivery {

    private Long id;
    private Long orderId;
    private String address;
    private String driverName;
    private String status;   // ASSIGNED, PICKED_UP, OUT_FOR_DELIVERY, DELIVERED

    public Delivery() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getDriverName() { return driverName; }
    public void setDriverName(String driverName) { this.driverName = driverName; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}

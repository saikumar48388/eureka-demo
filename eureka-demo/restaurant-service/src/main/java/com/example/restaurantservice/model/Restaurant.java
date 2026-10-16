package com.example.restaurantservice.model;

public class Restaurant {

    private Long id;
    private String name;
    private String cuisine;
    private String city;

    public Restaurant() {
    }

    public Restaurant(Long id, String name, String cuisine, String city) {
        this.id = id;
        this.name = name;
        this.cuisine = cuisine;
        this.city = city;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCuisine() {
        return cuisine;
    }

    public void setCuisine(String cuisine) {
        this.cuisine = cuisine;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }
}

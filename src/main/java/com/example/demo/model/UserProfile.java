package com.example.demo.model;

import java.util.List;

public record UserProfile(User user, List<Order> orders, List<Product> recommendations) {
}

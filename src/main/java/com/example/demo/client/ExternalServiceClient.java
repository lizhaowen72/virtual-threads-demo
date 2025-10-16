package com.example.demo.client;

import com.example.demo.model.Order;
import com.example.demo.model.Product;
import com.example.demo.model.User;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class ExternalServiceClient {
    // 模拟数据库调用 - 阻塞 I/O
    public User fetchUser(String userId) throws InterruptedException {
        Thread.sleep(100 + ThreadLocalRandom.current().nextInt(100)); // 模拟 I/O 延迟
        return new User(userId, "User " + userId, "user" + userId + "@example.com");
    }

    // 模拟订单服务调用
    public List<Order> fetchUserOrders(String userId) throws InterruptedException {
        Thread.sleep(150 + ThreadLocalRandom.current().nextInt(150));
        return List.of(
                new Order("order1", userId, 99.99, "completed"),
                new Order("order2", userId, 149.99, "pending")
        );
    }

    // 模拟推荐服务调用
    public List<Product> fetchRecommendations(String userId) throws InterruptedException {
        Thread.sleep(200 + ThreadLocalRandom.current().nextInt(200));
        return List.of(
                new Product("prod1", "Laptop", "Electronics"),
                new Product("prod2", "Book", "Education"),
                new Product("prod3", "Headphones", "Electronics")
        );
    }

    // 模拟支付服务调用
    public String fetchPaymentStatus(String orderId) throws InterruptedException {
        Thread.sleep(80 + ThreadLocalRandom.current().nextInt(80));
        return ThreadLocalRandom.current().nextBoolean() ? "PAID" : "PENDING";
    }

}

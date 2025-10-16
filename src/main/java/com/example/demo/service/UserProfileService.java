package com.example.demo.service;

import com.example.demo.client.ExternalServiceClient;
import com.example.demo.model.*;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.StructuredTaskScope.Subtask;

@Service
public class UserProfileService {
    private final ExternalServiceClient externalServiceClient;
    private final Executor virtualThreadExecutor;

    public UserProfileService(ExternalServiceClient externalServiceClient) {
        this.externalServiceClient = externalServiceClient;
        this.virtualThreadExecutor = Executors.newVirtualThreadPerTaskExecutor();
    }

    /**
     * 传统方式 - 顺序执行（性能较差）
     */
    public UserProfile getUserProfileSequential(String userId) throws InterruptedException {
        User user = externalServiceClient.fetchUser(userId);
        List<Order> orders = externalServiceClient.fetchUserOrders(userId);
        List<Product> recommendations = externalServiceClient.fetchRecommendations(userId);

        return new UserProfile(user, orders, recommendations);
    }

    /**
     * 使用 CompletableFuture 和虚拟线程
     */
    public CompletableFuture<UserProfile> getUserProfileAsync(String userId) {
        CompletableFuture<User> userFuture = CompletableFuture
                .supplyAsync(() -> {
                    try {
                        return externalServiceClient.fetchUser(userId);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }, virtualThreadExecutor);

        CompletableFuture<List<Order>> ordersFuture = CompletableFuture
                .supplyAsync(() -> {
                    try {
                        return externalServiceClient.fetchUserOrders(userId);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }, virtualThreadExecutor);

        CompletableFuture<List<Product>> recommendationsFuture = CompletableFuture
                .supplyAsync(() -> {
                    try {
                        return externalServiceClient.fetchRecommendations(userId);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }, virtualThreadExecutor);

        return CompletableFuture.allOf(userFuture, ordersFuture, recommendationsFuture)
                .thenApply(ignored -> new UserProfile(
                        userFuture.join(),
                        ordersFuture.join(),
                        recommendationsFuture.join()
                ));
    }

    /**
     * 使用结构化并发 - 推荐方式
     */
    public UserProfile getUserProfileStructured(String userId) {
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {

            Subtask<User> userSubtask = scope.fork(() ->
                    externalServiceClient.fetchUser(userId)
            );

            Subtask<List<Order>> ordersSubtask = scope.fork(() ->
                    externalServiceClient.fetchUserOrders(userId)
            );

            Subtask<List<Product>> recommendationsSubtask = scope.fork(() ->
                    externalServiceClient.fetchRecommendations(userId)
            );

            // 等待所有任务完成
            scope.join();
            // 检查是否有任务失败
            scope.throwIfFailed();

            return new UserProfile(
                    userSubtask.get(),
                    ordersSubtask.get(),
                    recommendationsSubtask.get()
            );

        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Operation interrupted", e);
        }
    }

    /**
     * 带超时的结构化并发
     */
    public UserProfile getUserProfileWithTimeout(String userId, Duration timeout) {
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {

            Subtask<User> userSubtask = scope.fork(() ->
                    externalServiceClient.fetchUser(userId)
            );

            Subtask<List<Order>> ordersSubtask = scope.fork(() ->
                    externalServiceClient.fetchUserOrders(userId)
            );

            Subtask<List<Product>> recommendationsSubtask = scope.fork(() ->
                    externalServiceClient.fetchRecommendations(userId)
            );

            // 带超时的等待
            scope.joinUntil(Instant.now().plus(timeout));
            scope.throwIfFailed();

            return new UserProfile(
                    userSubtask.get(),
                    ordersSubtask.get(),
                    recommendationsSubtask.get()
            );

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Operation interrupted", e);
        } catch (TimeoutException e) {
            throw new RuntimeException("Operation timed out after " + timeout, e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 复杂场景：嵌套的结构化并发
     */
    public UserProfile getUserProfileComplex(String userId) {
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {

            Subtask<User> userSubtask = scope.fork(() ->
                    externalServiceClient.fetchUser(userId)
            );

            Subtask<List<Order>> ordersSubtask = scope.fork(() ->
                    fetchOrdersWithPaymentStatus(userId)  // 嵌套并发调用
            );

            Subtask<List<Product>> recommendationsSubtask = scope.fork(() ->
                    externalServiceClient.fetchRecommendations(userId)
            );

            scope.join();
            scope.throwIfFailed();

            return new UserProfile(
                    userSubtask.get(),
                    ordersSubtask.get(),
                    recommendationsSubtask.get()
            );

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Operation interrupted", e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
    private List<Order> fetchOrdersWithPaymentStatus(String userId) throws InterruptedException, ExecutionException {
        // 获取订单
        List<Order> orders = externalServiceClient.fetchUserOrders(userId);

        // 为每个订单并行获取支付状态
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {

            @SuppressWarnings("unchecked")
            Subtask<String>[] statusSubtasks = orders.stream()
                    .map(order -> scope.fork(() ->
                            externalServiceClient.fetchPaymentStatus(order.id())
                    ))
                    .toArray(Subtask[]::new);

            scope.join();
            scope.throwIfFailed();

            // 创建带有支付状态的订单列表
            return orders.stream()
                    .map(order -> {
                        int index = orders.indexOf(order);
                        String paymentStatus = statusSubtasks[index].get();
                        return new Order(
                                order.id(),
                                order.userId(),
                                order.amount(),
                                order.status() + " (" + paymentStatus + ")"
                        );
                    })
                    .toList();
        }
    }
}

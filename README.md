# Spring Boot Virtual Threads & Structured Concurrency Demo

A demonstration project showcasing how to leverage Java 21's Virtual Threads and Structured Concurrency in Spring Boot REST services for handling multiple I/O-bound operations efficiently.

## 🚀 Features

- **Virtual Threads**: Lightweight threads for high-concurrency I/O operations
- **Structured Concurrency**: Managed task lifecycle with automatic error propagation
- **Multiple Concurrency Patterns**: Sequential, async, structured, and timeout-based approaches
- **Performance Monitoring**: Execution time tracking for comparison
- **RESTful API**: Clean endpoints to test different concurrency strategies

## 🛠 Technologies

- **Java 21** (Virtual Threads & Structured Concurrency)
- **Spring Boot 3.2+**
- **Maven**
- **Virtual Threads Executor**
- **StructuredTaskScope** (Preview feature)

## 📋 Prerequisites

- JDK 21 or later
- Maven 3.6+
- Spring Boot 3.2+

## ⚙️ Configuration

### Enable Virtual Threads

In `application.properties`:
```properties
spring.threads.virtual.enabled=true
```

### Or via Java Config

```java
@Bean
public AsyncTaskExecutor applicationTaskExecutor() {
    return new TaskExecutorAdapter(Executors.newVirtualThreadPerTaskExecutor());
}
```

### Enable Preview Features

Add to your `pom.xml` for Structured Concurrency support:

```xml
<compilerArgs>--enable-preview</compilerArgs>
```

## 🏃‍♂️ Running the Application

1. **Clone the repository**
   ```bash
   git clone <your-repository-url>
   cd virtual-threads-demo
   ```

2. **Build and run**
   ```bash
   mvn spring-boot:run
   ```

3. **The application will start on** `http://localhost:8080`

## 📊 API Endpoints

### 1. Sequential Execution (Baseline)
```bash
curl "http://localhost:8080/api/users/123/profile/sequential"
```
- Executes I/O operations one after another
- Slowest but simplest approach

### 2. Async with Virtual Threads
```bash
curl "http://localhost:8080/api/users/123/profile/async"
```
- Uses `CompletableFuture` with virtual threads
- Better performance than sequential

### 3. Structured Concurrency
```bash
curl "http://localhost:8080/api/users/123/profile/structured"
```
- Uses `StructuredTaskScope.ShutdownOnFailure`
- Automatic error propagation and cancellation

### 4. Structured Concurrency with Timeout
```bash
curl "http://localhost:8080/api/users/123/profile/structured-timeout"
```
- Adds timeout control to structured concurrency
- Prevents long-running operations

### 5. Complex Nested Concurrency
```bash
curl "http://localhost:8080/api/users/123/profile/complex"
```
- Demonstrates nested structured concurrency
- Combines multiple concurrent operations

## 🔍 Performance Comparison

Check the `X-Execution-Time` header in responses to compare performance:

```bash
curl -i "http://localhost:8080/api/users/123/profile/sequential"
curl -i "http://localhost:8080/api/users/123/profile/async"
curl -i "http://localhost:8080/api/users/123/profile/structured"
```

Typical results:
- **Sequential**: ~400-600ms
- **Concurrent**: ~150-250ms (2-3x faster)

## 🏗 Project Structure

```
src/main/java/com/example/demo/
├── config/
│   └── VirtualThreadConfig.java      # Virtual threads configuration
├── controller/
│   └── UserController.java           # REST endpoints
├── service/
│   └── UserProfileService.java       # Business logic with concurrency patterns
├── client/
│   └── ExternalServiceClient.java    # Mock external service calls
└── model/
    └── *.java                        # Data models (User, Order, Product, etc.)
```

## 💡 Key Concepts

### Virtual Threads
- Lightweight threads managed by JVM
- Perfect for I/O-bound operations
- Can create millions without memory issues
- Automatic suspension during I/O waits

### Structured Concurrency
- Organizes concurrent tasks into hierarchical structures
- Automatic cancellation propagation
- Clean resource management
- Better error handling

### Benefits in REST Services
- **Improved throughput**: Handle more concurrent requests
- **Better resource utilization**: Efficient I/O waiting
- **Cleaner code**: Simplified concurrency management
- **Enhanced reliability**: Automatic error handling

## 🐛 Troubleshooting

### Common Issues

1. **Preview Features Error**
   ```bash
   # Enable preview features in Maven
   mvn compile -Dmaven.compiler.args="--enable-preview"
   ```

2. **Virtual Threads Not Working**
   - Verify Java 21 is being used: `java -version`
   - Check Spring Boot version (3.2+ required)

3. **Missing Response Headers**
   ```bash
   # Use -i flag to see all headers
   curl -i "http://localhost:8080/api/users/123/profile/structured"
   ```

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## 📝 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 🙏 Acknowledgments

- Java 21 Virtual Threads & Structured Concurrency
- Spring Boot Team for virtual threads integration
- OpenJDK for innovative concurrency features

---

**Happy Coding!** 🎉

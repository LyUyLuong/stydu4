# Logging - Hướng dẫn sử dụng

## 📋 Tổng quan

Project có 3 loại log:
1. **`logs/stydu4.log`** - Tất cả logs
2. **`logs/error.log`** - Chỉ errors
3. **Console** - Màu sắc dễ đọc

## 🎯 Tính năng

- ✅ Tự động log Controller và Service methods
- ✅ Hiển thị thời gian chạy (ms)
- ✅ Cảnh báo nếu method chạy lâu (>1s)
- ✅ Log SQL queries
- ✅ File tự động rotate theo ngày

---

## 📝 Cách dùng

### 1. Tự động log (LoggingAspect)

Controller và Service tự động được log:

```java
@RestController
public class UserController {
    
    @GetMapping("/{id}")
    public User getUser(@PathVariable String id) {
        // Tự động log:
        // ==> Controller: getUser
        // <== Controller: getUser completed in 42ms
        return userService.getUserById(id);
    }
}
```

### 2. Log thủ công

Thêm `@Slf4j` vào class:

```java
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class MyService {
    
    public void doSomething() {
        log.debug("Chi tiết debug");
        log.info("Thông tin chính");
        log.warn("Cảnh báo");
        log.error("Lỗi", exception);
    }
}
```

### 3. Log Levels

| Level | Khi nào dùng |
|-------|--------------|
| DEBUG | Development, chi tiết |
| INFO  | Thông tin quan trọng |
| WARN  | Cảnh báo, chưa phải lỗi |
| ERROR | Lỗi nghiêm trọng |

---

## ⚙️ Cấu hình

### Thay đổi log level (application.yaml)

```yaml
logging:
  level:
    com.lul.Stydu4: DEBUG        # Code của bạn
    org.hibernate.SQL: DEBUG     # SQL queries
    org.springframework.web: INFO # Spring framework
```

### Production

Tạo file `application-prod.yaml`:

```yaml
logging:
  level:
    com.lul.Stydu4: INFO         # Ít log hơn
    org.hibernate.SQL: WARN      # Tắt SQL
```

---

## 📊 Xem logs

### Real-time:
```bash
# Windows
Get-Content logs\stydu4.log -Wait

# Linux/Mac
tail -f logs/stydu4.log
```

### Tìm errors:
```bash
# Windows
Select-String "ERROR" logs\stydu4.log

# Linux/Mac
grep "ERROR" logs/stydu4.log
```

---

## 💡 Tips

### ✅ NÊN:
- Log các sự kiện quan trọng (login, tạo order, etc.)
- Log errors với exception
- Dùng DEBUG cho development

### ❌ KHÔNG NÊN:
- Log passwords
- Log trong vòng lặp lớn
- Log object quá lớn

### Ví dụ tốt:
```java
log.info("User logged in - userId={}", userId);
log.error("Payment failed - orderId={}", orderId, exception);
```

### Ví dụ xấu:
```java
log.info("User: " + user); // Object quá lớn
log.info("Password: " + password); // BẮT BUỘC KHÔNG được log!
```

---

## 🛠️ Troubleshooting

**Logs quá nhiều?**
- Đổi DEBUG → INFO trong `application.yaml`

**Performance chậm?**
- Tắt SQL logging: `org.hibernate.SQL: WARN`

**Không thấy logs?**
- Check thư mục `logs/` có được tạo không
- Check quyền write

---

Last updated: October 24, 2025


### ✅ Các loại log files:
1. **`stydu4.log`** - Tất cả logs (INFO level trở lên)
2. **`stydu4-error.log`** - Chỉ errors (ERROR level)
3. **`stydu4-security.log`** - Security events (login, logout, access denied)
4. **`stydu4-performance.log`** - Performance metrics (slow queries, API response time)

### ✅ Tính năng:
- ✅ Tự động log tất cả Controller, Service, Repository methods
- ✅ Log request/response HTTP với headers
- ✅ Performance tracking (thời gian thực thi)
- ✅ Security event logging
- ✅ Slow query detection
- ✅ File rotation tự động (10MB/file, lưu 30 ngày)
- ✅ Async logging để không ảnh hưởng performance
- ✅ Màu sắc dễ đọc trên console

---

## 📁 Cấu trúc Log Files

```
logs/
├── stydu4.log                      # Current log file
├── stydu4-error.log                # Current error log
├── stydu4-security.log             # Current security log
├── stydu4-performance.log          # Current performance log
└── archived/                       # Archived logs (auto-compressed)
    ├── stydu4-2025-10-23.0.log.gz
    ├── stydu4-2025-10-24.0.log.gz
    └── ...
```

---

## 🎯 Cách sử dụng

### 1. Automatic Logging (LoggingAspect)

**Controllers, Services, Repositories** tự động được log:

```java
@RestController
@RequestMapping("/users")
public class UserController {
    
    @GetMapping("/{userId}")
    public ApiResponse<UserResponse> getUser(@PathVariable String userId) {
        // Tự động log:
        // → Controller: UserController.getUser() called with args: [123]
        // ← Controller: UserController.getUser() completed in 45ms
        return userService.getUserById(userId);
    }
}
```

### 2. Security Logging

```java
@Service
public class AuthenticationService {
    
    @Autowired
    private SecurityLogger securityLogger;
    
    public void login(String username, String password, HttpServletRequest request) {
        try {
            // Login logic...
            String ipAddress = request.getRemoteAddr();
            securityLogger.logLoginSuccess(username, ipAddress);
            // ✓ LOGIN SUCCESS - User: john@example.com | IP: 192.168.1.1
        } catch (Exception e) {
            securityLogger.logLoginFailure(username, ipAddress, e.getMessage());
            // ✗ LOGIN FAILED - User: john@example.com | IP: 192.168.1.1 | Reason: Invalid password
        }
    }
}
```

**Available security log methods:**
```java
securityLogger.logLoginSuccess(username, ipAddress);
securityLogger.logLoginFailure(username, ipAddress, reason);
securityLogger.logLogout(username);
securityLogger.logTokenRefresh(username);
securityLogger.logAccessDenied(username, resource, action);
securityLogger.logSuspiciousActivity(username, ipAddress, activity);
securityLogger.logOAuth2Login(email, provider);
securityLogger.logPasswordChange(username);
securityLogger.logRoleChange(username, oldRole, newRole);
```

### 3. Performance Logging

```java
@Service
public class TestService {
    
    @Autowired
    private PerformanceLogger performanceLogger;
    
    public void uploadFile(MultipartFile file) {
        long startTime = System.currentTimeMillis();
        
        // Upload logic...
        
        long duration = System.currentTimeMillis() - startTime;
        performanceLogger.logFileUpload(
            file.getOriginalFilename(), 
            file.getSize(), 
            duration
        );
        // 📤 FILE UPLOAD - File: test.mp3 | Size: 5.2MB | Time: 1200ms | Speed: 4.3MB/s
    }
}
```

**Available performance log methods:**
```java
performanceLogger.logDatabaseQuery(query, executionTimeMs);
performanceLogger.logApiEndpoint(endpoint, method, executionTimeMs);
performanceLogger.logFileUpload(filename, sizeBytes, uploadTimeMs);
performanceLogger.logCacheHit(cacheName, key);
performanceLogger.logCacheMiss(cacheName, key);
performanceLogger.logRedisOperation(operation, key, executionTimeMs);
performanceLogger.logMemoryUsage();
```

### 4. Manual Logging (Using @Slf4j)

```java
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class MyService {
    
    public void doSomething() {
        log.debug("Debug message - detailed info");
        log.info("Info message - general info");
        log.warn("Warning message - something unusual");
        log.error("Error message - something went wrong", exception);
    }
}
```

---

## 📊 Log Levels

| Level | Khi nào dùng | Ví dụ |
|-------|--------------|-------|
| **TRACE** | Chi tiết nhất, debugging sâu | SQL parameters |
| **DEBUG** | Development debugging | Method calls, variable values |
| **INFO** | Thông tin quan trọng | User login, API calls |
| **WARN** | Cảnh báo, không phải lỗi | Slow query, deprecated usage |
| **ERROR** | Lỗi nghiêm trọng | Exceptions, failures |

---

## 🎨 Log Format

### Console (có màu):
```
2025-10-24 10:30:45.123  INFO [http-nio-8080-exec-1] UserController : → Controller: UserController.getUser() called with args: [123]
2025-10-24 10:30:45.168  INFO [http-nio-8080-exec-1] UserService    : ← Service: UserService.getUserById() completed in 42ms
```

### File (plain text):
```
2025-10-24 10:30:45.123  INFO [http-nio-8080-exec-1] c.l.S.controller.UserController : → Controller: UserController.getUser() called
2025-10-24 10:30:45.168  INFO [http-nio-8080-exec-1] c.l.S.service.impl.UserServiceImpl : ← Service: UserService.getUserById() completed
```

---

## ⚙️ Cấu hình Logging

### Development (application.yaml)
```yaml
logging:
  level:
    com.lul.Stydu4: DEBUG          # Your code - DEBUG level
    org.springframework.web: INFO   # Spring - INFO level
    org.hibernate.SQL: DEBUG        # Show SQL queries
```

### Production (application-prod.yaml)
Tạo file `application-prod.yaml`:
```yaml
logging:
  level:
    com.lul.Stydu4: INFO           # Your code - INFO only
    org.springframework.web: WARN   # Spring - WARN only
    org.hibernate.SQL: WARN         # Hide SQL queries
  file:
    path: /var/log/stydu4           # Production log path
```

---

## 🔧 Tùy chỉnh Logging

### Tắt auto-logging cho một class cụ thể:

```java
@Service
@Slf4j
public class SensitiveService {
    // LoggingAspect vẫn chạy, nhưng có thể exclude bằng cách:
    // Thêm annotation @NoLogging (tự tạo)
}
```

### Thay đổi threshold cho slow query:

Trong `LoggingAspect.java`:
```java
// Từ 500ms -> 1000ms
if (stopWatch.getTotalTimeMillis() > 1000) {
    log.warn("⚠ Repository: slow query detected");
}
```

### Thay đổi log file size:

Trong `logback-spring.xml`:
```xml
<maxFileSize>20MB</maxFileSize>  <!-- Từ 10MB -> 20MB -->
<maxHistory>60</maxHistory>      <!-- Từ 30 ngày -> 60 ngày -->
```

---

## 📈 Monitoring Tips

### 1. Xem logs real-time:
```bash
# Linux/Mac
tail -f logs/stydu4.log

# Windows PowerShell
Get-Content logs\stydu4.log -Wait
```

### 2. Tìm errors:
```bash
# Linux/Mac
grep "ERROR" logs/stydu4.log

# Windows PowerShell
Select-String "ERROR" logs\stydu4.log
```

### 3. Tìm slow queries:
```bash
grep "SLOW QUERY" logs/stydu4-performance.log
```

### 4. Tìm failed logins:
```bash
grep "LOGIN FAILED" logs/stydu4-security.log
```

---

## 🚀 Best Practices

### ✅ DO:
- Log important business events (login, order created, payment processed)
- Log errors với stack trace
- Log slow queries/APIs
- Use structured logging (key=value pairs)
- Log với context (userId, transactionId, etc.)

### ❌ DON'T:
- Log passwords hoặc sensitive data
- Log trong tight loops (performance issue)
- Log quá nhiều ở DEBUG level trong production
- Log entire objects (có thể rất lớn)
- Forget to handle exceptions

### Example - Good Logging:
```java
log.info("User order created - userId={}, orderId={}, amount={}", 
         userId, orderId, amount);

try {
    processPayment(order);
} catch (PaymentException e) {
    log.error("Payment failed - orderId={}, userId={}, error={}", 
              order.getId(), order.getUserId(), e.getMessage(), e);
}
```

### Example - Bad Logging:
```java
log.debug("Order: " + order); // Don't log entire object
log.info("Password: " + password); // NEVER log passwords!
```

---

## 📊 Log Emojis Reference

Để dễ đọc hơn, logs sử dụng emojis:

- ✓ Success
- ✗ Error/Failure
- → Incoming/Start
- ← Outgoing/Complete
- ↻ Refresh/Redirect
- ⚠️ Warning
- 🐌 Slow performance
- 🚨 Security alert
- 📤 Upload
- 📥 Download
- 💾 Cache/Memory
- 🔑 Password/Auth
- 👤 User/Role
- ⛔ Access denied

---

## 🛠️ Troubleshooting

### Log files không được tạo?
1. Check quyền write vào thư mục `logs/`
2. Check `application.yaml` có cấu hình `logging.file.path`
3. Check Logback config trong `logback-spring.xml`

### Logs quá nhiều, làm đầy disk?
1. Giảm `maxHistory` trong `logback-spring.xml`
2. Tăng compression rate
3. Set up log rotation service
4. Chuyển DEBUG -> INFO trong production

### Performance bị ảnh hưởng?
1. Sử dụng Async appenders (đã enable)
2. Giảm logging level
3. Disable HTTP request/response logging nếu không cần

---

## 📚 Tài liệu tham khảo

- [Logback Documentation](https://logback.qos.ch/documentation.html)
- [SLF4J Manual](http://www.slf4j.org/manual.html)
- [Spring Boot Logging](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.logging)

---

Last updated: October 24, 2025

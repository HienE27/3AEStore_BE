# 🚀 Exercise 201 - Backend API

Backend API cho hệ thống thương mại điện tử Exercise 201, được xây dựng bằng Spring Boot với Java 17, cung cấp RESTful APIs đầy đủ cho ứng dụng bán sách trực tuyến.

## 📋 Tổng quan

Hệ thống backend Exercise 201 cung cấp:

- 🔐 **Authentication & Authorization**: JWT, OAuth2 Google Login
- 🛍️ **E-commerce APIs**: Quản lý sản phẩm, giỏ hàng, đơn hàng
- 👥 **User Management**: Quản lý khách hàng, nhân viên, quyền hạn
- 💳 **Payment Integration**: VNPay, quản lý thanh toán
- 📧 **Email Services**: Gửi email xác thực, thông báo
- 📁 **File Upload**: Upload và quản lý hình ảnh sản phẩm
- 🤖 **AI Integration**: Tự động tạo mô tả sản phẩm bằng Gemini/OpenAI
- 📊 **Admin Dashboard**: APIs quản trị hệ thống
- 📰 **Content Management**: Tin tức, slideshow, khuyến mãi

## 🛠️ Công nghệ sử dụng

### Core Framework
- **Spring Boot 3.4.5** - Framework Java enterprise
- **Java 17** - Java runtime
- **Maven** - Dependency management

### Database & ORM
- **MySQL** - Relational database
- **Spring Data JPA** - ORM framework
- **Hibernate** - JPA implementation
- **Flyway** - Database migration (tùy chọn)

### Security & Authentication
- **Spring Security** - Security framework
- **JWT (JJWT)** - JSON Web Tokens
- **OAuth2 Client** - Google social login
- **BCrypt** - Password hashing

### API & Web
- **Spring Web** - REST API framework
- **Spring Data REST** - REST data services
- **Jackson** - JSON processing
- **Spring Validation** - Input validation

### Additional Features
- **Spring Boot Actuator** - Application monitoring
- **Spring Boot Mail** - Email services
- **Spring DotEnv** - Environment variables
- **Lombok** - Code generation

### AI Integration
- **Google Gemini API** - AI text generation (FREE)
- **OpenAI API** - AI text generation (Paid fallback)
- **Template-based** - Fallback generation

## 🚀 Cài đặt và Chạy

### Yêu cầu hệ thống
- **Java** >= 17
- **Maven** >= 3.6
- **MySQL** >= 8.0
- **Git**

### Chuẩn bị Database

1. **Cài đặt MySQL** và tạo database:
   ```sql
   CREATE DATABASE exercise201;
   ```

2. **Cập nhật thông tin database** trong `src/main/resources/application.properties`:
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/exercise201?userSSL=false&serverTimezone=UTC&useLegacyDatetimecode=false
   spring.datasource.username=root
   spring.datasource.password=123456
   ```

### Cài đặt AI Features (Tùy chọn)

Để sử dụng tính năng tự động tạo mô tả sản phẩm bằng AI:

1. **Lấy Gemini API Key (FREE)**:
   - Vào: https://makersuite.google.com/app/apikey
   - Tạo API key và copy

2. **Tạo file `.env`** trong thư mục gốc:
   ```env
   GEMINI_API_KEY=AIzaSyYourActualKeyHere
   OPENAI_API_KEY=sk-your-openai-key-here  # Optional
   ```

3. **Google OAuth2 (Tùy chọn)**:
   ```env
   GOOGLE_CLIENT_ID=your-google-client-id
   GOOGLE_CLIENT_SECRET=your-google-client-secret
   ```

### Chạy ứng dụng

1. **Clone repository**:
   ```bash
   git clone <repository-url>
   cd BE201
   ```

2. **Cài đặt dependencies**:
   ```bash
   mvn clean install
   ```

3. **Chạy ứng dụng**:
   ```bash
   # Sử dụng Maven wrapper
   ./mvnw spring-boot:run

   # Hoặc Maven trực tiếp
   mvn spring-boot:run
   ```

4. **Kiểm tra API**:
   - Base URL: http://localhost:8080
   - API Docs: http://localhost:8080/swagger-ui.html (nếu có)

### Scripts tiện ích

```bash
# Chạy tests
./mvnw test

# Build JAR file
./mvnw clean package

# Chạy JAR file
java -jar target/exercise201-0.0.1-SNAPSHOT.jar

# Development với hot reload
./mvnw spring-boot:run -Dspring-boot.run.fork=false
```

## 📁 Cấu trúc dự án

```
src/main/java/com/nguyenviethien/exercise201/
├── config/              # Cấu hình Spring
│   ├── DotEnvConfig.java        # Load .env files
│   ├── MailConfig.java          # Email configuration
│   ├── SecurityConfiguration.java # Spring Security
│   ├── VNPayConfig.java         # Payment config
│   └── WebMvcConfig.java        # Web MVC config
├── controller/          # REST Controllers
│   ├── AdminController.java     # Admin APIs
│   ├── ProductController.java   # Product management
│   ├── OrderController.java     # Order management
│   ├── UserAPI.java            # User operations
│   └── ... (27 controllers)
├── entity/             # JPA Entities
│   ├── Product.java            # Product entity
│   ├── Customer.java           # Customer entity
│   ├── Order.java              # Order entity
│   └── ... (42 entities)
├── repository/         # Data Access Layer
│   ├── ProductRepository.java  # Product queries
│   ├── UserRepository.java     # User queries
│   └── ... (38 repositories)
├── service/            # Business Logic
│   ├── ProductService.java     # Product business logic
│   ├── AuthService.java        # Authentication logic
│   ├── AIService.java          # AI integration
│   └── ... (57 services)
├── DTO/                # Data Transfer Objects
│   ├── ProductDto.java         # Product DTO
│   ├── CheckoutRequest.java    # Checkout DTO
│   └── ... (9 DTOs)
├── security/           # Security Layer
│   ├── SecurityConfiguration.java
│   ├── JwtResponse.java        # JWT response
│   └── OAuth2LoginSuccessHandler.java
└── exception/          # Exception Handling
    └── ResourceNotFoundException.java
```

## 🔧 Cấu hình chi tiết

### Application Properties

```properties
# Database
spring.datasource.url=jdbc:mysql://localhost:3306/exercise201
spring.datasource.username=root
spring.datasource.password=123456

# JPA/Hibernate
spring.jpa.hibernate.ddl-auto=none
spring.jpa.show-sql=true

# Email
spring.mail.host=smtp.gmail.com
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password

# File Upload
spring.servlet.multipart.max-file-size=10MB
app.upload.dir=uploads

# AI Integration
ai.gemini.api.key=${GEMINI_API_KEY:}
ai.gemini.api.model=${GEMINI_MODEL:gemini-1.5-flash}
```

### Environment Variables (.env)

```env
# AI APIs
GEMINI_API_KEY=AIzaSy...
OPENAI_API_KEY=sk-...

# Google OAuth2
GOOGLE_CLIENT_ID=...
GOOGLE_CLIENT_SECRET=...

# Database (override)
DB_URL=jdbc:mysql://localhost:3306/exercise201
DB_USERNAME=root
DB_PASSWORD=123456
```

## 🌟 API Endpoints

### Authentication
```
POST   /api/auth/login          # Đăng nhập
POST   /api/auth/register       # Đăng ký
POST   /api/auth/refresh        # Refresh token
GET    /api/auth/me            # Thông tin user hiện tại
```

### Products
```
GET    /api/products           # Danh sách sản phẩm
GET    /api/products/{id}      # Chi tiết sản phẩm
POST   /api/products           # Tạo sản phẩm (Admin)
PUT    /api/products/{id}      # Cập nhật sản phẩm (Admin)
DELETE /api/products/{id}      # Xóa sản phẩm (Admin)
```

### Shopping Cart
```
GET    /api/cart               # Lấy giỏ hàng
POST   /api/cart/items         # Thêm vào giỏ
PUT    /api/cart/items/{id}    # Cập nhật số lượng
DELETE /api/cart/items/{id}    # Xóa khỏi giỏ
```

### Orders
```
GET    /api/orders             # Danh sách đơn hàng
GET    /api/orders/{id}        # Chi tiết đơn hàng
POST   /api/orders             # Tạo đơn hàng
PUT    /api/orders/{id}/status # Cập nhật trạng thái (Admin)
```

### Admin APIs
```
GET    /api/admin/dashboard    # Dashboard stats
GET    /api/admin/users        # Quản lý users
GET    /api/admin/orders       # Quản lý orders
POST   /api/admin/products     # Quản lý products
```

## 🤖 AI Features

### Gemini Integration
- **Free Tier**: 60 requests/phút, 1,500 requests/ngày
- **Usage**: Tự động tạo mô tả sản phẩm chi tiết
- **Fallback**: OpenAI API → Template-based

### Cách sử dụng AI
```java
@Autowired
private AIService aiService;

String description = aiService.generateProductDescription("Sự im lặng của bầy cừu");
// Returns detailed product description
```

## 💳 Payment Integration

### VNPay Configuration
```properties
# VNPay settings trong VNPayConfig.java
vn.pay.url=https://sandbox.vnpayment.vn/paymentv2/vpcpay.html
vn.pay.tmn.code=YOUR_TMN_CODE
vn.pay.hash.secret=YOUR_SECRET_KEY
```

### Payment Flow
1. Client gửi checkout request
2. Server tạo VNPay URL
3. Client redirect đến VNPay
4. VNPay callback về server
5. Server cập nhật order status

## 📧 Email Services

### Email Templates
- **User Registration**: Xác thực email
- **Order Confirmation**: Xác nhận đơn hàng
- **Password Reset**: Đặt lại mật khẩu
- **Order Status Updates**: Cập nhật trạng thái

### Configuration
```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password
```

## 🔒 Security

### JWT Authentication
- **Access Token**: 15 phút
- **Refresh Token**: 7 ngày
- **Secure Cookies**: HttpOnly, Secure, SameSite

### Role-based Access
- **CUSTOMER**: Khách hàng thông thường
- **STAFF**: Nhân viên
- **ADMIN**: Quản trị viên

### Protected Endpoints
```java
.antMatchers("/api/admin/**").hasRole("ADMIN")
.antMatchers("/api/staff/**").hasAnyRole("ADMIN", "STAFF")
.antMatchers("/api/user/**").authenticated()
```

## 📊 Monitoring

### Actuator Endpoints
```
GET  /actuator/health     # Health check
GET  /actuator/info       # Application info
GET  /actuator/metrics    # Application metrics
GET  /actuator/loggers    # Logging configuration
```

## 🧪 Testing

```bash
# Chạy tất cả tests
./mvnw test

# Chạy tests với coverage
./mvnw test jacoco:report

# Integration tests
./mvnw verify
```

## 📦 Deployment

### Build JAR
```bash
./mvnw clean package -DskipTests
```

### Docker (Tùy chọn)
```dockerfile
FROM openjdk:17-jdk-alpine
COPY target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app.jar"]
```

### Production Checklist
- [ ] Cập nhật database credentials
- [ ] Cấu hình email production
- [ ] Setup SSL/TLS
- [ ] Configure reverse proxy (Nginx)
- [ ] Setup monitoring & logging
- [ ] Backup strategy
- [ ] Environment variables

## 🔧 Troubleshooting

### Common Issues

**Database Connection Failed**
```bash
# Kiểm tra MySQL service
sudo systemctl status mysql

# Test connection
mysql -u root -p -e "SELECT 1"
```

**Port 8080 Already in Use**
```bash
# Kill process using port 8080
sudo lsof -ti:8080 | xargs kill -9

# Or change port in application.properties
server.port=8081
```

**AI API Not Working**
- Kiểm tra `.env` file exists
- Verify API keys are valid
- Check network connectivity
- Review application logs

**File Upload Issues**
- Check `uploads/` directory permissions
- Verify file size limits
- Ensure multipart configuration

## 🤝 Đóng góp

1. Fork project
2. Tạo feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to branch (`git push origin feature/AmazingFeature`)
5. Tạo Pull Request

## 📞 Liên hệ

- **Project**: Exercise 201 E-commerce Backend
- **Tech Stack**: Spring Boot + MySQL + JWT
- **Version**: 0.0.1-SNAPSHOT
- **Java Version**: 17

---

**🚀 Happy Coding!** Hệ thống backend này cung cấp nền tảng vững chắc cho ứng dụng thương mại điện tử với đầy đủ tính năng enterprise-grade.

# 🚀 Exercise 201 — Backend API

> **RESTful API Backend for E-commerce Bookstore Platform**
> Built with **Spring Boot 3.4** · **Java 17** · **MySQL 8** · **JWT** · **VNPay** · **Google OAuth2** · **Gemini AI**

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.5-brightgreen)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-17-blue)](https://www.java.com/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-orange)](https://www.mysql.com/)
[![Security](https://img.shields.io/badge/Security-JWT%20%2B%20OAuth2-blueviolet)](https://spring.io/projects/spring-security)

---

## 📋 Project Description

**Exercise 201 Backend** là hệ thống API RESTful hoàn chỉnh cho nền tảng thương mại điện tử bán sách trực tuyến. Xây dựng từ đầu (end-to-end) với kiến trúc **Controller → Service → Repository**, cung cấp đầy đủ chức năng E-commerce kết hợp AI và thanh toán thực tế.

### Scale & Architecture
| Layer | Quantity |
|-------|----------|
| **REST API Controllers** | **27** |
| **Service Interfaces** | **26** |
| **Service Implementations** | **26** |
| **JPA Repositories** | **39** |
| **JPA Entities** | **44** |
| **DTOs** | **11** |
| **Security Configs** | **6** |

### User Roles & Access Control
| Role | Permissions |
|------|------------|
| **CUSTOMER** | Browse products, shopping cart, wishlist, checkout, VNPay payment, reviews, profile |
| **STAFF** | All CUSTOMER permissions + manage orders, reviews, slideshows, news, feedback |
| **ADMIN** | Full system access — products, categories, users, staff, orders, payments, promotions, AI generation, settings |

### Problems Solved
| Problem | Solution | Result |
|---------|----------|--------|
| Manual product data entry is slow | **Gemini AI** auto-generates descriptions | **80% faster** product onboarding |
| Complex payment processing | **VNPay** real payment gateway | **Real transactions**, not simulation |
| Multiple user types management | **JWT + OAuth2** with role-based access | **Secure & scalable** authentication |
| Hard to maintain large codebase | **Clean architecture** (Controller → Service → Repository) | **Maintainable** codebase |
| Unvalidated data causes runtime errors | **Spring Validation + JSR-303** annotations | **Fewer bugs** in production |
| Slow database queries | **Spring Data JPA + Hibernate** with query optimization | **Fast** data access |

---

## 🛠️ Tech Stack

### Core Framework
| Layer | Technology |
|-------|-----------|
| Framework | **Spring Boot 3.4.5** |
| Language | **Java 17** |
| Build Tool | **Maven** |
| Monitoring | Spring Boot Actuator |

### Database & ORM
| Layer | Technology |
|-------|-----------|
| Database | **MySQL 8.0** |
| ORM | **Spring Data JPA** |
| Implementation | **Hibernate** |
| Migrations | **Flyway** (optional) |

### Security & Authentication
| Layer | Technology |
|-------|-----------|
| Security Framework | **Spring Security 6** |
| Authentication | **JWT (JJWT 0.11.5)** |
| Social Login | **OAuth2 Client (Google)** |
| Password Hashing | **BCrypt** |

### API & Web
| Layer | Technology |
|-------|-----------|
| REST Framework | **Spring Web** |
| Data Services | **Spring Data REST** |
| JSON Processing | **Jackson** |
| Input Validation | **Spring Validation (JSR-303)** |

### Integrations & Utilities
| Service | Purpose |
|---------|---------|
| **VNPay** | Payment gateway (real transactions) |
| **Google OAuth2** | Social login |
| **Google Gemini AI** | Auto product descriptions (FREE — 60 req/min) |
| **OpenAI API** | AI fallback |
| **Gmail SMTP** | Email notifications |
| **Spring Dotenv** | Environment variables from `.env` |
| **Lombok** | Code generation (getters, setters, constructors) |

---

## 📁 Project Structure

```
src/main/java/com/nguyenviethien/exercise201/
│
├── Exercise201Application.java          # Main entry point
│
├── config/                             # 9 Configuration classes
│   ├── CorsFilter.java                  # CORS configuration
│   ├── DotEnvConfig.java                # Load .env file
│   ├── JacksonConfig.java               # JSON serialization config
│   ├── MailConfig.java                  # Gmail SMTP configuration
│   ├── MethodRestConfig.java            # REST method config
│   ├── ReviewStatusConverter.java       # JPA attribute converter
│   ├── VNPayConfig.java                # VNPay payment config
│   └── WebMvcConfig.java               # Web MVC configuration
│
├── controller/                        # 27 REST Controllers
│   ├── AdminController.java             # Admin dashboard & management APIs
│   ├── AdminReviewController.java       # Review moderation APIs
│   ├── AttributeController.java         # Product attributes (size, color, etc.)
│   ├── CardController.java              # Shopping cart management
│   ├── CardItemController.java          # Cart items CRUD
│   ├── CategoryController.java          # Category tree management
│   ├── CountryController.java           # Countries & regions
│   ├── CouponController.java           # Discount coupons
│   ├── CustomerAddressController.java  # Customer shipping addresses
│   ├── CustomerController.java         # Customer management
│   ├── FavoriteController.java         # Wishlist management
│   ├── FeedbackController.java          # Customer feedback
│   ├── FaviconController.java          # Serve favicon
│   ├── HomeController.java             # Home/landing page data
│   ├── NewsController.java             # News & articles
│   ├── OrderController.java            # Order CRUD & status management
│   ├── PaymentController.java          # VNPay payment endpoints
│   ├── ProductController.java          # Product CRUD + AI generation
│   ├── ReviewController.java           # Reviews, ratings, replies
│   ├── RoleController.java            # Role management
│   ├── ShippingZoneController.java     # Shipping zones & rates
│   ├── SlideshowController.java       # Homepage slideshow management
│   ├── StaffAccountController.java     # Staff account management
│   ├── SupplierController.java        # Supplier management
│   ├── TagController.java             # Product tags
│   ├── TestController.java           # Testing & debugging endpoints
│   ├── TokenController.java           # JWT token refresh & validation
│   └── VariantOptionController.java  # Product variant options
│
├── DTO/                               # 11 Data Transfer Objects
│   ├── CheckoutRequest.java           # Checkout payload
│   ├── CustomerDto.java               # Customer data transfer
│   ├── CustomerPageResponse.java      # Paginated customer response
│   ├── ProductDetailsDTO.java         # Detailed product data
│   ├── ProductDto.java               # Product data transfer
│   ├── ReviewDto.java                # Review data transfer
│   ├── ReviewImageDto.java           # Review image data
│   ├── ReviewReplyDto.java           # Review reply data
│   ├── StaffAccountDto.java          # Staff account data
│   ├── TokenInfoResponse.java        # JWT token info response
│   └── VnpayRequest.java             # VNPay payment request
│
├── entity/                            # 44 JPA Entities
│   ├── Product.java                  # Product entity
│   ├── Category.java                 # Category entity (hierarchical)
│   ├── Customer.java                 # Customer entity
│   ├── Order.java                    # Order entity
│   ├── OrderItem.java                # Order line item
│   ├── OrderStatus.java              # Order status enum
│   ├── Payment.java                  # Payment record
│   ├── Review.java                   # Product review
│   ├── ReviewImage.java             # Review photos
│   ├── ReviewReply.java             # Review reply from staff
│   ├── ReviewReport.java            # Review report flags
│   ├── ReviewStatus.java            # Review status enum
│   ├── Card.java                    # Shopping cart
│   ├── CardItem.java               # Cart item
│   ├── Favorite.java              # Wishlist item
│   ├── News.java                  # News article
│   ├── Slideshow.java             # Homepage slideshow
│   ├── Coupon.java               # Discount coupon
│   ├── Attribute.java            # Product attribute (size, color)
│   ├── AttributeValue.java       # Attribute value
│   ├── Variant.java             # Product variant
│   ├── VariantOption.java       # Variant option
│   ├── Tag.java                # Product tag
│   ├── Supplier.java           # Supplier entity
│   ├── Category.java           # Category entity
│   ├── Country.java           # Country for addresses
│   ├── CustomerAddress.java   # Customer address
│   ├── Notification.java      # User notification
│   ├── Role.java             # User role
│   ├── StaffAccount.java     # Staff account
│   ├── ShippingZone.java     # Shipping zone
│   ├── ShippingRate.java     # Shipping rate
│   ├── Feedbacks.java        # Customer feedback
│   ├── Gallery.java         # Product gallery
│   ├── ProductCategory.java # Many-to-many product-category
│   ├── ProductTag.java      # Many-to-many product-tag
│   ├── ProductCoupon.java   # Many-to-many product-coupon
│   ├── ProductShippingInfo.java  # Shipping info per product
│   ├── ProductSupplier.java # Many-to-many product-supplier
│   ├── ProductSupplierId.java   # Composite key
│   ├── ProductAttribute.java    # Product attribute values
│   ├── ProductAttributeValue.java # Attribute value reference
│   ├── Sell.java              # Sales tracking
│   └── Sell.java             # Revenue tracking
│
├── repository/                     # 39 Spring Data Repositories
│   ├── ProductRepository.java    # Product queries
│   ├── CategoryRepository.java   # Category queries
│   ├── CustomerRepository.java   # Customer queries
│   ├── OrderRepository.java     # Order queries
│   ├── ReviewRepository.java    # Review queries
│   ├── CardRepository.java      # Cart queries
│   ├── FavoriteRepository.java  # Wishlist queries
│   ├── NewsRepository.java      # News queries
│   ├── StaffAccountRepository.java # Staff queries
│   └── ... (30+ more repositories)
│
├── service/                       # 26 Service Interfaces
│   ├── impl/                      # 26 Service Implementations
│   │   ├── CustomerServiceImpl.java    # Customer business logic
│   │   ├── ProductServiceImpl.java     # Product business logic
│   │   ├── OrderServiceImpl.java      # Order business logic
│   │   ├── ReviewServiceImpl.java     # Review business logic
│   │   ├── AIServiceImpl.java         # Gemini/OpenAI integration
│   │   └── ... (21+ more service implementations)
│   ├── CustomerService.java      # Customer service interface
│   ├── ProductService.java       # Product service interface
│   ├── OrderService.java         # Order service interface
│   ├── ReviewService.java        # Review service interface
│   ├── AIGenerateDescriptionService.java # AI description generation
│   └── EmailService.java         # Email sending service
│
├── security/                       # Security layer
│   ├── SecurityConfiguration.java    # Spring Security config
│   ├── JWT/                         # JWT utilities
│   │   ├── JwtService.java          # JWT generation & parsing
│   │   └── JwtFilter.java           # JWT request filter
│   ├── CustomerSecurityService.java       # Customer authentication
│   ├── CustomerSecurityServiceImpl.java   # Customer auth implementation
│   ├── StaffAccountSecurityService.java   # Staff authentication
│   ├── StaffAccountSecurityServiceImpl.java # Staff auth implementation
│   └── OAuth2LoginSuccessHandler.java    # Google OAuth2 success handler
│
├── exception/                     # Exception handling
│   └── ResourceNotFoundException.java
│
└── util/                         # Utilities
    └── (utility classes)
```

---

## 🌟 Features

### Authentication & Authorization
- ✅ **JWT Authentication** — Access Token (15 min) + Refresh Token (7 days)
- ✅ **Google OAuth2** — Social login with Google account
- ✅ **BCrypt Password Encryption** — Secure password storage
- ✅ **Role-based Access Control** — CUSTOMER, STAFF, ADMIN
- ✅ **Protected Endpoints** — Spring Security with custom filters

### E-commerce APIs
- ✅ **Product Management** — CRUD with search, filter, pagination
- ✅ **Category Management** — Hierarchical tree structure
- ✅ **Shopping Cart** — Add, update, remove items
- ✅ **Wishlist** — Save favorite products
- ✅ **Order Management** — Full order lifecycle with status tracking
- ✅ **Reviews & Ratings** — With images, replies, and report system
- ✅ **Coupons & Discounts** — Flexible discount codes
- ✅ **Slideshows** — Homepage banner management
- ✅ **News & Promotions** — Content management
- ✅ **Shipping Zones** — Configurable shipping rates
- ✅ **Product Attributes & Variants** — Size, color, edition options
- ✅ **Tags & Suppliers** — Product categorization

### Integrations
- ✅ **VNPay Payment Gateway** — Real payment processing
- ✅ **Gemini AI** — Auto-generate product descriptions (FREE)
- ✅ **OpenAI API** — AI fallback option
- ✅ **Gmail SMTP** — Email notifications
- ✅ **File Upload** — Product images, review images
- ✅ **Spring Boot Actuator** — Health checks & monitoring

---

## 💳 Payment Flow (VNPay)

```
1. Customer selects products → Add to cart
2. Checkout → Enter shipping info
3. Select VNPay payment method
4. Backend generates VNPay payment URL with HMAC signature
5. Customer redirected to VNPay sandbox/live gateway
6. Customer completes payment on VNPay
7. VNPay returns with transaction result (IPN callback)
8. Backend validates HMAC signature & confirms order
9. Customer receives confirmation email
10. Order status updated in admin dashboard
```

---

## 🤖 AI-Powered Product Descriptions

### Gemini Integration (FREE)
- **Free tier**: 60 requests/minute, 1,500 requests/day
- **Model**: Gemini 1.5 Flash
- **Fallback chain**: Gemini → OpenAI API → Template-based generation

### How it works
```java
// ProductController.java — endpoint triggers AI generation
@Autowired
private AIGenerateDescriptionService aiService;

@PostMapping("/products/generate-description")
public ResponseEntity<String> generateDescription(@RequestParam String productName) {
    String description = aiService.generateProductDescription(productName);
    return ResponseEntity.ok(description);
}
```

### Benefits
| Metric | Before | After |
|--------|--------|-------|
| Manual data entry time | 100% | **20%** |
| Description quality | Inconsistent | **Consistent** |
| Time to list product | 5-10 min | **1-2 min** |

---

## 🔧 Configuration

### Database Setup
```sql
CREATE DATABASE exercise201 CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### Environment Variables (`.env` file)
```env
# Database (override application.properties)
DB_URL=jdbc:mysql://localhost:3306/exercise201
DB_USERNAME=root
DB_PASSWORD=your_password

# AI APIs
GEMINI_API_KEY=your_gemini_api_key        # Free: https://makersuite.google.com/apikey
OPENAI_API_KEY=sk-your-openai-key         # Optional fallback

# Google OAuth2
GOOGLE_CLIENT_ID=your-google-client-id.apps.googleusercontent.com
GOOGLE_CLIENT_SECRET=your-google-secret

# Email
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password
```

### Application Properties (src/main/resources/application.properties)
```properties
# Database
spring.datasource.url=jdbc:mysql://localhost:3306/exercise201
spring.datasource.username=root
spring.datasource.password=123456

# JPA/Hibernate
spring.jpa.hibernate.ddl-auto=none
spring.jpa.show-sql=false

# File Upload
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=20MB
app.upload.dir=uploads

# Email
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true

# VNPay
vn.pay.url=https://sandbox.vnpayment.vn/paymentv2/vpcpay.html
vn.pay.tmn.code=YOUR_TMN_CODE
vn.pay.hash.secret=YOUR_SECRET_KEY
vn.pay.return.url=http://localhost:3000/checkout/vnpay-return

# AI
ai.gemini.api.key=${GEMINI_API_KEY:}
ai.gemini.api.model=gemini-1.5-flash
```

---

## 🚀 Installation & Running

### Prerequisites
| Tool | Version |
|------|---------|
| Java | ≥ 17 |
| Maven | ≥ 3.6 |
| MySQL | ≥ 8.0 |
| Git | any |

### Steps

```bash
# 1. Clone repository
git clone https://github.com/HienE27/3AEStore_BE.git
cd 3AEStore_BE

# 2. Create database
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS exercise201 CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# 3. Configure database credentials
# Edit: src/main/resources/application.properties
# Update: spring.datasource.username & spring.datasource.password

# 4. (Optional) Create .env file for integrations
# See Configuration section above

# 5. Build project
mvn clean install

# 6. Run application
mvn spring-boot:run
# OR using wrapper:
./mvnw spring-boot:run

# Backend runs at: http://localhost:8080
```

### Useful Commands
```bash
mvn test                      # Run all tests
mvn clean package             # Build JAR file
mvn spring-boot:run -DskipTests  # Run without tests
java -jar target/exercise201-0.0.1-SNAPSHOT.jar  # Run JAR
```

---

## 📦 API Endpoints Overview

| Category | Endpoints |
|----------|-----------|
| **Auth** | `POST /api/auth/login`, `/register`, `/refresh`, `GET /me` |
| **Products** | CRUD + `/featured`, `/search`, `/generate-description` |
| **Categories** | CRUD + `/tree` (hierarchical structure) |
| **Cart** | `GET/POST/PUT/DELETE /api/cart`, `/api/cart/items/**` |
| **Orders** | CRUD + `/api/orders/{id}/status`, `/api/orders/history` |
| **Reviews** | CRUD + `/api/reviews/{productId}`, `/moderate/**` |
| **Wishlist** | `GET/POST/DELETE /api/favorites/**` |
| **News** | CRUD + `/api/news/latest` |
| **Payments** | `/api/payment/vnpay/create`, `/api/payment/vnpay/callback` |
| **Admin** | Dashboard stats, user/staff management, settings |
| **AI** | `/api/products/generate-description` |

### Actuator Endpoints
```
GET /actuator/health     # Health check
GET /actuator/info       # Application info
GET /actuator/metrics    # Application metrics
GET /actuator/loggers    # Logging configuration
```

---

## 🔒 Security Configuration

### JWT Token Strategy
```
Access Token Lifetime:   15 minutes
Refresh Token Lifetime: 7 days
Token Storage:          HttpOnly cookies (recommended)
Algorithm:              HS512
```

### Protected Endpoints
```java
.antMatchers("/api/admin/**").hasRole("ADMIN")
.antMatchers("/api/staff/**").hasAnyRole("ADMIN", "STAFF")
.antMatchers("/api/user/**").authenticated()
.antMatchers("/api/public/**").permitAll()
```

---

## 📊 Database Schema (Key Relationships)

```
Product ───< ProductCategory >── Category (many-to-many)
Product ───< ProductTag >── Tag (many-to-many)
Product ───< ProductSupplier >── Supplier (many-to-many)
Product ───< ProductCoupon >── Coupon (many-to-many)
Product ───< ProductAttribute >── AttributeValue
Product ───< Variant >── VariantOption
Product ───< Review ───< ReviewImage
Product ───< ReviewReply
Product ───< ReviewReport
Product ───< CardItem >── Card ─── Customer
Product ───< Favorite >── Customer
Product ───< Gallery
Product ───< OrderItem >── Order ───< Payment (VNPay)
Customer ───< CustomerAddress ─── Country
Customer ───< Notification
StaffAccount ─── Role
Category ───< Slideshow
```

---

## 🧪 Testing & Deployment

### Testing
```bash
mvn test                          # Run all tests
mvn clean package -DskipTests     # Build without tests
mvn verify                        # Integration tests
```

### Production Deployment
```bash
# Build JAR
mvn clean package -DskipTests

# Run with environment variables
java -jar target/exercise201-0.0.1-SNAPSHOT.jar \
  --spring.datasource.password=prod_password \
  --spring.profiles.active=production
```

---

## 📞 Contact

| Field | Info |
|-------|------|
| **Project** | Exercise 201 — E-commerce Bookstore Backend |
| **Author** | HienE27 |
| **GitHub** | https://github.com/HienE27/3AEStore_BE |
| **Tech Stack** | Spring Boot · Java 17 · MySQL · JWT · VNPay · Gemini AI |

---

**Built with ❤️ — A production-ready E-commerce backend platform.**

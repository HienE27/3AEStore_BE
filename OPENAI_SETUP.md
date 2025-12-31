# Hướng dẫn cấu hình OpenAI API để Generate Description tự động

## Tổng quan

Tính năng AI Generate Description cho phép tự động tạo mô tả chi tiết cho sách dựa trên tên sách. Hệ thống hỗ trợ 2 chế độ:

1. **OpenAI API** (Khuyến nghị): Sử dụng GPT-3.5 để tạo mô tả thông minh và phù hợp
2. **Template-based** (Fallback): Tạo mô tả dựa trên template cố định khi không có API key

## Cách cấu hình OpenAI API

### Bước 1: Đăng ký tài khoản OpenAI

1. Truy cập https://platform.openai.com/
2. Đăng ký tài khoản mới hoặc đăng nhập
3. Xác thực email và thông tin thanh toán (cần thẻ tín dụng để sử dụng API)

### Bước 2: Tạo API Key

1. Đăng nhập vào https://platform.openai.com/
2. Vào **API keys** trong menu bên trái
3. Click **Create new secret key**
4. Đặt tên cho key (ví dụ: "3AEStore Description Generator")
5. Copy API key ngay lập tức (sẽ không hiển thị lại sau khi đóng)

⚠️ **Lưu ý**: Giữ bí mật API key của bạn, không commit vào Git!

### Bước 3: Cấu hình trong application.properties

Mở file `src/main/resources/application.properties` và thêm dòng sau:

```properties
# OpenAI API Configuration
ai.openai.api.key=sk-your-actual-api-key-here
ai.openai.api.url=https://api.openai.com/v1/chat/completions
```

**Ví dụ:**
```properties
ai.openai.api.key=sk-proj-abc123xyz789...
ai.openai.api.url=https://api.openai.com/v1/chat/completions
```

### Bước 4: Restart ứng dụng

Sau khi cấu hình, restart Spring Boot application để áp dụng thay đổi.

## Kiểm tra cấu hình

### Cách 1: Kiểm tra trong code

Khi không có API key hoặc API key không hợp lệ, hệ thống sẽ tự động sử dụng template-based generation và log ra console:

```
Error calling OpenAI API: ...
```

### Cách 2: Test API endpoint

Gửi request POST đến endpoint:
```
POST http://localhost:8080/api/products/generate-description
Content-Type: application/json

{
  "productName": "Đắc Nhân Tâm"
}
```

**Nếu cấu hình đúng:**
- Response sẽ có mô tả chi tiết được tạo bởi AI
- Mô tả sẽ phù hợp với tên sách và có định dạng HTML

**Nếu chưa cấu hình hoặc lỗi:**
- Response vẫn có mô tả nhưng là template cố định
- Console sẽ hiển thị lỗi nếu có

## Chi phí sử dụng OpenAI API

- **Model**: GPT-3.5-turbo
- **Giá**: ~$0.002 per 1K tokens (rất rẻ)
- **Mỗi lần generate**: ~500 tokens = ~$0.001 (khoảng 25 VNĐ)
- **Free tier**: OpenAI cung cấp $5 credit miễn phí cho tài khoản mới

## Bảo mật

⚠️ **QUAN TRỌNG**: 

1. **KHÔNG** commit API key vào Git
2. Thêm `application.properties` vào `.gitignore` nếu chứa API key
3. Hoặc sử dụng environment variables:

```bash
# Windows PowerShell
$env:AI_OPENAI_API_KEY="sk-your-key-here"

# Linux/Mac
export AI_OPENAI_API_KEY="sk-your-key-here"
```

Sau đó trong `application.properties`:
```properties
ai.openai.api.key=${AI_OPENAI_API_KEY:}
```

## Troubleshooting

### Lỗi: "Invalid API key"
- Kiểm tra lại API key đã copy đúng chưa
- Đảm bảo API key còn hiệu lực (chưa bị xóa hoặc revoke)

### Lỗi: "Insufficient quota"
- Kiểm tra số dư tài khoản OpenAI
- Nạp thêm credit nếu cần

### Lỗi: "Network timeout"
- Kiểm tra kết nối internet
- Kiểm tra firewall có chặn kết nối đến api.openai.com không

### Fallback luôn được sử dụng
- Kiểm tra API key có được set đúng trong `application.properties` không
- Kiểm tra log console để xem lỗi cụ thể
- Đảm bảo đã restart ứng dụng sau khi thay đổi config

## Tùy chỉnh prompt

Để thay đổi cách AI generate description, sửa prompt trong file:
`src/main/java/com/nguyenviethien/exercise201/service/AIGenerateDescriptionService.java`

Dòng 58-61:
```java
message.put("content", "Hãy viết một mô tả chi tiết và hấp dẫn về cuốn sách có tên: \"" + productName + "\". " +
        "Mô tả nên bao gồm: giới thiệu về nội dung sách, đối tượng độc giả phù hợp, điểm nổi bật của cuốn sách. " +
        "Trả về kết quả dưới dạng HTML với các thẻ <p>, <ul>, <li> để định dạng đẹp. " +
        "Độ dài khoảng 200-300 từ.");
```

## Hỗ trợ

Nếu gặp vấn đề, kiểm tra:
1. Log console của Spring Boot application
2. Network tab trong browser DevTools khi gọi API
3. OpenAI API status: https://status.openai.com/


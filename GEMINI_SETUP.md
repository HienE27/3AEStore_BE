# Hướng dẫn cấu hình Google Gemini API (MIỄN PHÍ)

## Tại sao nên dùng Gemini?

- ✅ **MIỄN PHÍ** với free tier hào phóng (60 requests/phút)
- ✅ **Hỗ trợ tiếng Việt tốt**
- ✅ **Không cần thẻ tín dụng** để bắt đầu
- ✅ **Chất lượng cao**, tương đương GPT-3.5

## Bước 1: Lấy Gemini API Key (MIỄN PHÍ)

1. Truy cập: https://makersuite.google.com/app/apikey
2. Đăng nhập bằng tài khoản Google
3. Click **"Create API Key"** hoặc **"Get API Key"**
4. Chọn project (hoặc tạo project mới)
5. Copy API key (dạng: `AIzaSy...`)

⚠️ **Lưu ý**: Giữ bí mật API key, không commit vào Git!

## Bước 2: Cấu hình trong file .env

Mở file `3AEStore_BE/.env` và thêm:

```env
# Google Gemini API (FREE - Recommended)
GEMINI_API_KEY=AIzaSyYourActualGeminiKeyHere
GEMINI_API_URL=https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent
```

**Ví dụ:**
```env
GEMINI_API_KEY=AIzaSyC1234567890abcdefghijklmnopqrstuvwxyz
GEMINI_API_URL=https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent
```

## Bước 3: Restart ứng dụng

Sau khi cấu hình, restart Spring Boot application:

1. Stop backend (Ctrl+C)
2. Chạy lại backend
3. Kiểm tra logs:
   ```
   🟢 Gemini API Key loaded: true
   🟢 Gemini API Key length: 39
   ```

## Bước 4: Test tính năng

1. Mở trang thêm sản phẩm: http://localhost:3000/management/add-product
2. Nhập tên sách (ví dụ: "Sự im lặng của bầy cừu")
3. Click **"🤖 Tạo mô tả AI"**
4. Đợi 3-5 giây
5. Mô tả chi tiết sẽ được tạo tự động!

## Kiểm tra logs

Khi tạo mô tả, backend sẽ log:

```
🔍 ========== AI Generate Description ==========
📖 Product Name: Sự im lặng của bầy cừu
🟢 Using Google Gemini API (Primary)
📤 Sending request to Gemini API...
📥 Response status: 200 OK
✅ Successfully generated description from Gemini (length: 1234 chars)
✅ Description generated successfully
```

## Giới hạn Free Tier

- **60 requests/phút**
- **1,500 requests/ngày**
- Đủ cho hầu hết các use case phát triển và production nhỏ

## Nếu cần nhiều hơn

Có thể nâng cấp lên paid tier hoặc kết hợp với OpenAI API (hệ thống tự động fallback).

## Troubleshooting

### API key không hoạt động?
- Kiểm tra API key có đúng format không (bắt đầu bằng `AIzaSy`)
- Kiểm tra API đã được enable trong Google Cloud Console
- Kiểm tra quota còn lại

### Vẫn dùng template?
- Kiểm tra logs: "🟢 Gemini API Key loaded: true"
- Nếu false, kiểm tra file `.env` có đúng format không
- Restart backend sau khi thay đổi `.env`

## So sánh với OpenAI

| Feature | Gemini | OpenAI |
|---------|--------|--------|
| Giá | **MIỄN PHÍ** | $0.002/1K tokens |
| Free tier | 60 req/min | Không |
| Tiếng Việt | ✅ Tốt | ✅ Tốt |
| Cần thẻ | ❌ Không | ✅ Có |
| Chất lượng | Tương đương GPT-3.5 | GPT-3.5/4 |

## Link tham khảo

- Gemini API Docs: https://ai.google.dev/docs
- Get API Key: https://makersuite.google.com/app/apikey
- Pricing: https://ai.google.dev/pricing


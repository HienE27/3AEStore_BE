# 🚀 Quick Start - Tạo mô tả sản phẩm bằng AI (MIỄN PHÍ)

## Bước 1: Lấy Gemini API Key (2 phút)

1. Vào: **https://makersuite.google.com/app/apikey**
2. Đăng nhập Google
3. Click **"Create API Key"**
4. Copy API key (dạng: `AIzaSy...`)

## Bước 2: Cập nhật file .env

Mở file `3AEStore_BE/.env` và thêm API key:

```env
GEMINI_API_KEY=AIzaSyYourActualKeyHere
```

**Ví dụ:**
```env
GEMINI_API_KEY=AIzaSyC1234567890abcdefghijklmnopqrstuvwxyz
```

## Bước 3: Restart Backend

```bash
# Stop backend (Ctrl+C)
# Chạy lại backend
```

Kiểm tra logs:
```
✅ Loading .env file from: D:\DAT5\3AEStore_BE\.env
📋 Loaded env var: GEMINI_API_KEY (length: 39)
🟢 Gemini API Key loaded: true
```

## Bước 4: Sử dụng

1. Vào trang thêm sản phẩm
2. Nhập tên sách: **"Sự im lặng của bầy cừu"**
3. Click **"🤖 Tạo mô tả AI"**
4. Đợi 3-5 giây
5. ✨ Mô tả chi tiết được tạo tự động!

## Kết quả

AI sẽ tạo mô tả chi tiết về cuốn sách, bao gồm:
- Giới thiệu tổng quan
- Nội dung chính/cốt truyện
- Thông điệp và bài học
- Đối tượng độc giả
- Điểm nổi bật

**Ví dụ output cho "Sự im lặng của bầy cừu":**
> Cuốn sách kinh điển về tâm lý tội phạm, kể về cuộc đối đầu giữa Clarice Starling và Hannibal Lecter...

## Giới hạn Free Tier

- 60 requests/phút
- 1,500 requests/ngày
- **Không cần thẻ tín dụng**

## Nếu cần thêm

Hệ thống tự động fallback:
1. Gemini API (free) ← **Ưu tiên**
2. OpenAI API (paid) ← Fallback
3. Template ← Fallback cuối

---

**Thời gian setup:** < 5 phút
**Chi phí:** MIỄN PHÍ


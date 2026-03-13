-- ============================================================
-- SCRIPT TẠO DATABASE VỚI ENCODING CHUẨN
-- Chạy trước khi import data
-- ============================================================

-- 1. Tạo database với encoding UTF-8mb4
DROP DATABASE IF EXISTS bookstore_db;
CREATE DATABASE bookstore_db 
    CHARACTER SET utf8mb4 
    COLLATE utf8mb4_0900_ai_ci;

USE bookstore_db;

-- 2. Thiết lập encoding cho session
SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

-- ============================================================
-- CÁC BẢNG ĐÃ ĐƯỢC ĐỊNH NGHĨA TRONG database.sql
-- (Giữ nguyên cấu trúc bảng)
-- ============================================================

-- ============================================================
-- FIX ENCODING CHO DATA EXISTING
-- Chạy sau khi import data
-- ============================================================

-- 1. SỬA BẢNG attribute_values
UPDATE `attribute_values` SET `attribute_value` = 'Giá trị 1', `color` = 'Đỏ' WHERE `attribute_value` LIKE '%GiÃ¡ trá»‹%' OR `color` LIKE '%ÄŽ%';
UPDATE `attribute_values` SET `attribute_value` = 'Giá trị 2', `color` = 'Xanh dương' WHERE `attribute_value` LIKE '%GiÃ¡ trá»‹%' OR `color` LIKE '%Xanh dÆ°Æ%';
UPDATE `attribute_values` SET `attribute_value` = 'Giá trị 3', `color` = 'Xanh lá cây' WHERE `attribute_value` LIKE '%GiÃ¡ trá»‹%' OR `color` LIKE '%Xanh lÃ¡ cÃ¢y%';
UPDATE `attribute_values` SET `attribute_value` = 'Giá trị 4', `color` = 'Vàng' WHERE `attribute_value` LIKE '%GiÃ¡ trá»‹%' OR `color` LIKE '%VÃ  ng%';
UPDATE `attribute_values` SET `attribute_value` = 'Giá trị 5', `color` = 'Đen' WHERE `attribute_value` LIKE '%GiÃ¡ trá»‹%' OR `color` LIKE '%ÄŽen%';

-- 2. SỬA BẢNG attributes
UPDATE `attributes` SET `attribute_name` = 'Màu sắc' WHERE `attribute_name` LIKE '%MÃ u sắc%';
UPDATE `attributes` SET `attribute_name` = 'Kích cỡ' WHERE `attribute_name` LIKE '%KÃ­ch cá»¡%';
UPDATE `attributes` SET `attribute_name` = 'Vật liệu' WHERE `attribute_name` LIKE '%Váº­t liá»‡u%';
UPDATE `attributes` SET `attribute_name` = 'Cân nặng' WHERE `attribute_name` LIKE '%CÃ¢n náº·ng%';
UPDATE `attributes` SET `attribute_name` = 'Thương hiệu' WHERE `attribute_name` LIKE '%ThÆ°Æ¡ng hiá»‡u%';

-- 3. SỬA BẢNG categories
UPDATE `categories` SET `category_name` = 'Tiểu thuyết' WHERE `category_name` LIKE '%Tiá»™u Thuyáº¿t%';
UPDATE `categories` SET `category_name` = 'Văn học' WHERE `category_name` LIKE '%VÄƒn há»%';
UPDATE `categories` SET `category_name` = 'Trinh thám' WHERE `category_name` LIKE '%Trinh thÃ¡m%';
UPDATE `categories` SET `category_name` = 'Truyện tranh' WHERE `category_name` LIKE '%Truyá»‡n Tranh%';
UPDATE `categories` SET `category_name` = 'Truyện ngắn' WHERE `category_name` LIKE '%Truyá»‡n Ngắn%';
UPDATE `categories` SET `category_name` = 'Lịch sử' WHERE `category_name` LIKE '%Lá»‹ch sá»%';
UPDATE `categories` SET `category_name` = 'Kinh dị' WHERE `category_name` LIKE '%Kinh dá»‹%';
UPDATE `categories` SET `category_name` = 'Toán học' WHERE `category_name` LIKE '%ToÃ¡n há»%';

-- 4. SỬA BẢNG customer_addresses
UPDATE `customer_addresses` SET `address_line1` = '11B, Thôn 7, xã Hòa Bắc, huyện Di Linh, tỉnh Lâm Đồng' WHERE `address_line1` LIKE '%ThÃ´n 7%';
UPDATE `customer_addresses` SET `city` = 'Lâm Đồng', `country` = 'Vietnam' WHERE `city` LIKE '%LÃ¢m Äá»“ng%' OR `country` LIKE '%Vietnam%';
UPDATE `customer_addresses` SET `address_line1` = '118 Mân Thiện, 119 Mân Thiện' WHERE `address_line1` LIKE '%man thiá»‡n%';
UPDATE `customer_addresses` SET `district` = 'Quận 9' WHERE `district` LIKE '%Quáº­n 9%';
UPDATE `customer_addresses` SET `city` = 'TP. Hồ Chí Minh' WHERE `city` LIKE '%TP. Há»“%';
UPDATE `customer_addresses` SET `ward` = 'Phường Hiệp Bình Chánh' WHERE `ward` LIKE '%phÆ°á»ng Hiá»‡p BÃ¬nh%';

-- 5. SỬA BẢNG customers
UPDATE `customers` SET `first_name` = 'Trần', `last_name` = 'Khôi' WHERE `first_name` LIKE '%Tráº§n%' AND `email` LIKE '%khoi123%';
UPDATE `customers` SET `first_name` = 'Nguyễn', `last_name` = 'Nam' WHERE `first_name` LIKE '%Nguyen%' AND `email` LIKE '%nam123%';
UPDATE `customers` SET `first_name` = 'Trần', `last_name` = 'Nga' WHERE `first_name` LIKE '%Tráº§n%' AND `email` LIKE '%nga123%';
UPDATE `customers` SET `first_name` = 'Nguyễn', `last_name` = 'Hiến' WHERE `last_name` LIKE '%Hiáº¿n%' AND `email` LIKE '%viethienhm%';

-- 6. SỬA BẢNG notifications
UPDATE `notifications` SET `content` = 'Đơn hàng của bạn đã được đặt thành công. Kiểm tra email của bạn để biết chi tiết.' WHERE `content` LIKE '%ÄÆơn hÃ ng%';
UPDATE `notifications` SET `content` = 'Lô hàng của bạn đã được gửi đi. Theo dõi gói hàng của bạn trực tuyến.' WHERE `content` LIKE '%LÃ´ hÃ ng%';
UPDATE `notifications` SET `title` = 'Xác nhận đơn hàng' WHERE `title` LIKE '%XÃ¡c nháº­n%';
UPDATE `notifications` SET `title` = 'Đã gửi hàng' WHERE `title` LIKE '%ÄÃ£ gá»­i%';

-- 7. SỬA BẢNG order_statuses
UPDATE `order_statuses` SET `color` = 'Đỏ' WHERE `color` LIKE '%ÄŽ%';
UPDATE `order_statuses` SET `color` = 'Xanh dương' WHERE `color` LIKE '%Xanh dÆ°Æ%';
UPDATE `order_statuses` SET `color` = 'Xanh lá cây' WHERE `color` LIKE '%Xanh lÃ¡ cÃ¢y%';
UPDATE `order_statuses` SET `color` = 'Vàng' WHERE `color` LIKE '%VÃ  ng%';
UPDATE `order_statuses` SET `color` = 'Đen' WHERE `color` LIKE '%ÄŽen%';

-- 8. SỬA BẢNG orders
UPDATE `orders` SET `shipping_address` = '22B, Đường 28, Phường Hiệp Bình Chánh, Thủ Đức' WHERE `shipping_address` LIKE '%Ä‘Æ°á»˜ng 28%';
UPDATE `orders` SET `note` = 'Không có ghi chú' WHERE `note` LIKE '%KhÃ´ng cÃ³ ghi chÃº%';

-- 9. SỬA BẢNG products
UPDATE `products` SET `short_description` = 'giảm giá' WHERE `short_description` LIKE '%giáº£m giÃ¡%';
UPDATE `products` SET `product_name` = 'Hoa Hướng Dương' WHERE `product_name` LIKE '%Hoa HÆ°á»›ng%';

-- ============================================================
-- FUNCTION CHUYỂN BINARY(16) SANG UUID
-- Giúp hiển thị id dạng chuẩn
-- ============================================================
DELIMITER //
CREATE FUNCTION fn_uuid(b BINARY(16))
RETURNS VARCHAR(36) DETERMINISTIC NO SQL
RETURN LOWER(CONCAT(
    HEX(SUBSTRING(b,1,4)),'-',
    HEX(SUBSTRING(b,5,2)),'-',
    HEX(SUBSTRING(b,7,2)),'-',
    HEX(SUBSTRING(b,9,2)),'-',
    HEX(SUBSTRING(b,11))
));
END //
DELIMITER ;

-- ============================================================
-- KIỂM TRA KẾT QUẢ
-- ============================================================
SELECT '✓ Đã sửa xong encoding!' AS status;

-- Kiểm tra còn lỗi không
SELECT 'categories' AS table_name, COUNT(*) AS errors FROM categories WHERE category_name LIKE '%Ã%' OR category_name LIKE '%Æ°%';
SELECT 'attributes' AS table_name, COUNT(*) AS errors FROM attributes WHERE attribute_name LIKE '%Ã%' OR attribute_name LIKE '%Æ°%';
SELECT 'products' AS table_name, COUNT(*) AS errors FROM products WHERE product_name LIKE '%Ã%' OR product_name LIKE '%Æ°%';

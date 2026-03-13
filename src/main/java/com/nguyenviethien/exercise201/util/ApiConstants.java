package com.nguyenviethien.exercise201.util;

public class ApiConstants {

    private ApiConstants() {
        // Prevent instantiation
    }

    // Success messages
    public static final String SUCCESS = "Thành công!";
    public static final String CREATE_SUCCESS = "Tạo mới thành công!";
    public static final String UPDATE_SUCCESS = "Cập nhật thành công!";
    public static final String DELETE_SUCCESS = "Xóa thành công!";
    public static final String SAVE_SUCCESS = "Lưu thành công!";

    // Error messages
    public static final String ERROR = "Có lỗi xảy ra!";
    public static final String VALIDATION_ERROR = "Dữ liệu không hợp lệ!";
    public static final String NOT_FOUND = "Không tìm thấy dữ liệu!";
    public static final String UNAUTHORIZED = "Chưa xác thực!";
    public static final String FORBIDDEN = "Không có quyền truy cập!";
    public static final String BAD_CREDENTIALS = "Tên đăng nhập hoặc mật khẩu không đúng!";

    // Product errors
    public static final String PRODUCT_NOT_FOUND = "Không tìm thấy sản phẩm!";
    public static final String PRODUCT_IN_ORDER = "Không thể xóa sản phẩm vì còn tồn tại trong đơn hàng!";
    public static final String PRODUCT_NAME_REQUIRED = "Tên sản phẩm là bắt buộc!";
    public static final String PRODUCT_PRICE_INVALID = "Giá sản phẩm không hợp lệ!";

    // Customer errors
    public static final String CUSTOMER_NOT_FOUND = "Không tìm thấy khách hàng!";
    public static final String EMAIL_EXISTS = "Email đã được sử dụng!";
    public static final String USERNAME_EXISTS = "Tên đăng nhập đã tồn tại!";
    public static final String ACCOUNT_NOT_ACTIVATED = "Tài khoản chưa được kích hoạt!";
    public static final String ACCOUNT_ALREADY_ACTIVATED = "Tài khoản đã được kích hoạt!";

    // Order errors
    public static final String ORDER_NOT_FOUND = "Không tìm thấy đơn hàng!";
    public static final String ORDER_STATUS_INVALID = "Trạng thái đơn hàng không hợp lệ!";

    // Category errors
    public static final String CATEGORY_NOT_FOUND = "Không tìm thấy danh mục!";

    // Payment errors
    public static final String PAYMENT_FAILED = "Thanh toán thất bại!";
    public static final String PAYMENT_CANCELLED = "Thanh toán đã bị hủy!";

    // Email errors
    public static final String EMAIL_SEND_FAILED = "Gửi email thất bại!";
    public static final String ACTIVATION_EMAIL_SUBJECT = "Mã kích hoạt tài khoản";

    // URLs
    public static final String FRONTEND_BASE_URL = "http://localhost:3000";

    // Pagination
    public static final int DEFAULT_PAGE_SIZE = 10;
    public static final int MAX_PAGE_SIZE = 100;
}

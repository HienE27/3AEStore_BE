package com.duongthuantri.exercise201.security;

public class Endpoints {
    public static final String FRONT_END_HOST = "http://localhost:3000";

    // Các endpoint công khai cho mọi người (nếu có)
    public static final String[] PUBLIC_GET = {
        // "/api/customers/login",
        // "/api/staff/login",
        "/public/**"  // Ví dụ thêm một endpoint công khai khác
    };

    public static final String[] PUBLIC_POST = {
        "/api/customers/login",
        "/api/staff/login", // Các endpoint login
    };

    public static final String[] ADMIN_GET = {
        "/admin/**",    // Tất cả các endpoint dưới /admin đều yêu cầu ADMIN quyền
    };

    public static final String[] ADMIN_POST = {
        // Các endpoint POST dành cho ADMIN
    };

    public static final String[] ADMIN_PUT = {
        // Các endpoint PUT dành cho ADMIN
    };

    public static final String[] ADMIN_DELETE = {
        // Các endpoint DELETE dành cho ADMIN
    };
}

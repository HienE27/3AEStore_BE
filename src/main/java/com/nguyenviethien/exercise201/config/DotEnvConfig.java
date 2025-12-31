package com.nguyenviethien.exercise201.config;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Load .env file và set các biến môi trường vào Spring Environment
 * Được gọi trước khi Spring Application khởi động
 */
public class DotEnvConfig implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        ConfigurableEnvironment environment = applicationContext.getEnvironment();
        
        // Chỉ đọc từ 1 file .env duy nhất: 3AEStore_BE/.env
        // Tìm file .env trong thư mục hiện tại (khi chạy từ 3AEStore_BE/)
        Path envPath = Paths.get(".env").toAbsolutePath().normalize();
        
        // Nếu không tìm thấy ở thư mục hiện tại, thử tìm trong 3AEStore_BE/
        if (!Files.exists(envPath)) {
            String workingDir = System.getProperty("user.dir");
            Path backendEnvPath = Paths.get(workingDir, "3AEStore_BE", ".env").toAbsolutePath().normalize();
            if (Files.exists(backendEnvPath)) {
                envPath = backendEnvPath;
            }
        }
        
        if (!Files.exists(envPath)) {
            System.out.println("⚠️ File .env not found at: " + envPath.toAbsolutePath());
            System.out.println("💡 Create .env file in 3AEStore_BE/ directory with GEMINI_API_KEY");
            return;
        }
        
        System.out.println("✅ Loading .env file from: " + envPath.toAbsolutePath());
        
        Map<String, Object> envProperties = new HashMap<>();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(envPath.toFile()))) {
            String line;
            
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                
                // Bỏ qua comment và dòng trống
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                
                // Parse key=value
                int equalIndex = line.indexOf('=');
                if (equalIndex > 0) {
                    String key = line.substring(0, equalIndex).trim();
                    String value = line.substring(equalIndex + 1).trim();
                    
                    // Loại bỏ quotes nếu có
                    if (value.startsWith("\"") && value.endsWith("\"")) {
                        value = value.substring(1, value.length() - 1);
                    }
                    if (value.startsWith("'") && value.endsWith("'")) {
                        value = value.substring(1, value.length() - 1);
                    }
                    
                    envProperties.put(key, value);
                    System.out.println("📋 Loaded env var: " + key + " (length: " + value.length() + ")");
                }
            }
            
            // Add properties to Spring environment
            MapPropertySource propertySource = new MapPropertySource("dotenv", envProperties);
            environment.getPropertySources().addFirst(propertySource);
            
            System.out.println("✅ Successfully loaded " + envProperties.size() + " environment variables from .env");
            
        } catch (IOException e) {
            System.err.println("❌ Error loading .env file: " + e.getMessage());
            e.printStackTrace();
        }
    }
}


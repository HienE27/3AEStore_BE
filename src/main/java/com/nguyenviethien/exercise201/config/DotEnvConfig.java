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
        // #region agent log
        try {
            java.io.FileWriter fw = new java.io.FileWriter("d:\\DAT5\\.cursor\\debug.log", true);
            fw.write("{\"id\":\"log_" + System.currentTimeMillis() + "_dotenv1\",\"timestamp\":" + System.currentTimeMillis() + ",\"location\":\"DotEnvConfig.java:24\",\"message\":\"DotEnvConfig.initialize called\",\"data\":{},\"sessionId\":\"debug-session\",\"runId\":\"run1\",\"hypothesisId\":\"A\"}\n");
            fw.close();
        } catch (IOException ex) {}
        // #endregion
        
        ConfigurableEnvironment environment = applicationContext.getEnvironment();
        
        // Tìm file .env trong thư mục hiện tại (working directory)
        // Khi chạy từ 3AEStore_BE/, .env sẽ là 3AEStore_BE/.env
        Path envPath = Paths.get(".env").toAbsolutePath().normalize();
        
        // Nếu không tìm thấy, thử tìm trong thư mục 3AEStore_BE
        if (!Files.exists(envPath)) {
            Path backendEnvPath = Paths.get("3AEStore_BE/.env").toAbsolutePath().normalize();
            if (Files.exists(backendEnvPath)) {
                envPath = backendEnvPath;
            }
        }
        
        // #region agent log
        try {
            java.io.FileWriter fw = new java.io.FileWriter("d:\\DAT5\\.cursor\\debug.log", true);
            fw.write("{\"id\":\"log_" + System.currentTimeMillis() + "_dotenv2\",\"timestamp\":" + System.currentTimeMillis() + ",\"location\":\"DotEnvConfig.java:37\",\"message\":\"Checking .env file\",\"data\":{\"path\":\"" + envPath.toString().replace("\\", "\\\\") + "\",\"exists\":\"" + Files.exists(envPath) + "\",\"workingDir\":\"" + System.getProperty("user.dir").replace("\\", "\\\\") + "\"},\"sessionId\":\"debug-session\",\"runId\":\"run1\",\"hypothesisId\":\"B\"}\n");
            fw.close();
        } catch (IOException ex) {}
        // #endregion
        
        if (!Files.exists(envPath)) {
            System.out.println("⚠️ File .env not found at: " + envPath.toAbsolutePath());
            // #region agent log
            try {
                java.io.FileWriter fw = new java.io.FileWriter("d:\\DAT5\\.cursor\\debug.log", true);
                fw.write("{\"id\":\"log_" + System.currentTimeMillis() + "_dotenv3\",\"timestamp\":" + System.currentTimeMillis() + ",\"location\":\"DotEnvConfig.java:33\",\"message\":\".env file not found, exiting\",\"data\":{},\"sessionId\":\"debug-session\",\"runId\":\"run1\",\"hypothesisId\":\"B\"}\n");
                fw.close();
            } catch (IOException ex) {}
            // #endregion
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
                    
                    // #region agent log
                    try {
                        java.io.FileWriter fw = new java.io.FileWriter("d:\\DAT5\\.cursor\\debug.log", true);
                        fw.write("{\"id\":\"log_" + System.currentTimeMillis() + "_dotenv4\",\"timestamp\":" + System.currentTimeMillis() + ",\"location\":\"DotEnvConfig.java:63\",\"message\":\"Parsed env variable\",\"data\":{\"key\":\"" + key + "\",\"valueLength\":\"" + value.length() + "\",\"valuePrefix\":\"" + (value.length() > 20 ? value.substring(0, 20) : value) + "\"},\"sessionId\":\"debug-session\",\"runId\":\"run1\",\"hypothesisId\":\"C\"}\n");
                        fw.close();
                    } catch (IOException ex2) {}
                    // #endregion
                }
            }
            
            // #region agent log
            try {
                java.io.FileWriter fw = new java.io.FileWriter("d:\\DAT5\\.cursor\\debug.log", true);
                fw.write("{\"id\":\"log_" + System.currentTimeMillis() + "_dotenv5\",\"timestamp\":" + System.currentTimeMillis() + ",\"location\":\"DotEnvConfig.java:69\",\"message\":\"Before adding to Spring environment\",\"data\":{\"propertiesCount\":\"" + envProperties.size() + "\"},\"sessionId\":\"debug-session\",\"runId\":\"run1\",\"hypothesisId\":\"D\"}\n");
                fw.close();
            } catch (IOException ex) {}
            // #endregion
            
            // Add properties to Spring environment
            MapPropertySource propertySource = new MapPropertySource("dotenv", envProperties);
            environment.getPropertySources().addFirst(propertySource);
            
            // #region agent log
            try {
                java.io.FileWriter fw = new java.io.FileWriter("d:\\DAT5\\.cursor\\debug.log", true);
                fw.write("{\"id\":\"log_" + System.currentTimeMillis() + "_dotenv6\",\"timestamp\":" + System.currentTimeMillis() + ",\"location\":\"DotEnvConfig.java:73\",\"message\":\"After adding to Spring environment\",\"data\":{\"propertiesCount\":\"" + envProperties.size() + "\"},\"sessionId\":\"debug-session\",\"runId\":\"run1\",\"hypothesisId\":\"D\"}\n");
                fw.close();
            } catch (IOException ex) {}
            // #endregion
            
            System.out.println("✅ Successfully loaded " + envProperties.size() + " environment variables from .env");
            
        } catch (IOException e) {
            System.err.println("❌ Error loading .env file: " + e.getMessage());
            e.printStackTrace();
        }
    }
}


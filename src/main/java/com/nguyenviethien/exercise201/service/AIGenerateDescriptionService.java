package com.nguyenviethien.exercise201.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import org.springframework.beans.factory.annotation.Value;
import java.util.HashMap;
import java.util.Map;

@Service
public class AIGenerateDescriptionService {
    
    @Value("${ai.openai.api.key:}")
    private String openAiApiKey;
    
    @Value("${ai.openai.api.url:https://api.openai.com/v1/chat/completions}")
    private String openAiApiUrl;
    
    private final RestTemplate restTemplate = new RestTemplate();
    
    /**
     * Generate product description using AI based on product name
     * @param productName The name of the product/book
     * @return Generated description in HTML format
     */
    public String generateDescription(String productName) {
        if (productName == null || productName.trim().isEmpty()) {
            return "";
        }
        
        // If OpenAI API key is not configured, use fallback template-based generation
        if (openAiApiKey == null || openAiApiKey.trim().isEmpty()) {
            return generateFallbackDescription(productName);
        }
        
        try {
            return generateWithOpenAI(productName);
        } catch (Exception e) {
            System.err.println("Error calling OpenAI API: " + e.getMessage());
            // Fallback to template-based generation
            return generateFallbackDescription(productName);
        }
    }
    
    /**
     * Generate description using OpenAI API
     */
    private String generateWithOpenAI(String productName) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiApiKey);
        
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-3.5-turbo");
        
        Map<String, Object> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", "Hãy viết một mô tả chi tiết và hấp dẫn về cuốn sách có tên: \"" + productName + "\". " +
                "Mô tả nên bao gồm: giới thiệu về nội dung sách, đối tượng độc giả phù hợp, điểm nổi bật của cuốn sách. " +
                "Trả về kết quả dưới dạng HTML với các thẻ <p>, <ul>, <li> để định dạng đẹp. " +
                "Độ dài khoảng 200-300 từ.");
        
        requestBody.put("messages", new Object[]{message});
        requestBody.put("max_tokens", 500);
        requestBody.put("temperature", 0.7);
        
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
        
        @SuppressWarnings("unchecked")
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
            openAiApiUrl,
            HttpMethod.POST,
            request,
            (Class<Map<String, Object>>) (Class<?>) Map.class
        );
        
        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            Map<String, Object> responseBody = response.getBody();
            if (responseBody.containsKey("choices")) {
                Object choicesObj = responseBody.get("choices");
                if (choicesObj instanceof java.util.List && !((java.util.List<?>) choicesObj).isEmpty()) {
                    Object firstChoice = ((java.util.List<?>) choicesObj).get(0);
                    if (firstChoice instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> choice = (Map<String, Object>) firstChoice;
                        if (choice.containsKey("message")) {
                            Object messageObj = choice.get("message");
                            if (messageObj instanceof Map) {
                                @SuppressWarnings("unchecked")
                                Map<String, Object> messageMap = (Map<String, Object>) messageObj;
                                if (messageMap.containsKey("content")) {
                                    return messageMap.get("content").toString();
                                }
                            }
                        }
                    }
                }
            }
        }
        
        return generateFallbackDescription(productName);
    }
    
    /**
     * Fallback: Generate description using template when AI API is not available
     */
    private String generateFallbackDescription(String productName) {
        StringBuilder description = new StringBuilder();
        description.append("<h3>Giới thiệu về cuốn sách</h3>");
        description.append("<p><strong>").append(productName).append("</strong> là một cuốn sách đáng đọc với nội dung phong phú và ý nghĩa sâu sắc.</p>");
        
        description.append("<h4>Nội dung chính:</h4>");
        description.append("<ul>");
        description.append("<li>Cuốn sách mang đến những kiến thức và trải nghiệm quý giá</li>");
        description.append("<li>Nội dung được trình bày một cách dễ hiểu và thu hút</li>");
        description.append("<li>Phù hợp với nhiều đối tượng độc giả khác nhau</li>");
        description.append("</ul>");
        
        description.append("<h4>Điểm nổi bật:</h4>");
        description.append("<p>Cuốn sách này sẽ giúp bạn có thêm những góc nhìn mới mẻ và kiến thức bổ ích. " +
                "Đây là một lựa chọn tuyệt vời cho những ai đang tìm kiếm một cuốn sách chất lượng và ý nghĩa.</p>");
        
        description.append("<p><em>Hãy đọc và khám phá những điều thú vị trong cuốn sách này!</em></p>");
        
        return description.toString();
    }
}


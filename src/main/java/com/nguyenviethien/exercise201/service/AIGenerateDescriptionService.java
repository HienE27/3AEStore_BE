package com.nguyenviethien.exercise201.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class AIGenerateDescriptionService {

    // Đọc từ environment variable OPENAI_API_KEY trước, nếu không có thì đọc từ
    // application.properties
    @Value("${OPENAI_API_KEY:${ai.openai.api.key:}}")
    private String openAiApiKey;

    // Đọc từ environment variable OPENAI_API_URL trước, nếu không có thì đọc từ
    // application.properties
    @Value("${OPENAI_API_URL:${ai.openai.api.url:https://api.openai.com/v1/chat/completions}}")
    private String openAiApiUrl;

    private final RestTemplate restTemplate;

    public AIGenerateDescriptionService() {
        // Configure RestTemplate with timeout
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout((int) TimeUnit.SECONDS.toMillis(10)); // 10 seconds connection timeout
        factory.setReadTimeout((int) TimeUnit.SECONDS.toMillis(30)); // 30 seconds read timeout
        this.restTemplate = new RestTemplate(factory);
    }

    // Post-construct để log sau khi Spring inject values
    @PostConstruct
    public void init() {
        // #region agent log
        try {
            java.io.FileWriter fw = new java.io.FileWriter("d:\\DAT5\\.cursor\\debug.log", true);
            fw.write("{\"id\":\"log_" + System.currentTimeMillis() + "_ai1\",\"timestamp\":"
                    + System.currentTimeMillis()
                    + ",\"location\":\"AIGenerateDescriptionService.java:38\",\"message\":\"AIGenerateDescriptionService @PostConstruct\",\"data\":{\"apiKeyIsNull\":\""
                    + (openAiApiKey == null) + "\",\"apiKeyIsEmpty\":\""
                    + (openAiApiKey != null && openAiApiKey.trim().isEmpty()) + "\",\"apiKeyLength\":\""
                    + (openAiApiKey != null ? openAiApiKey.length() : 0) + "\",\"apiUrl\":\"" + openAiApiUrl
                    + "\"},\"sessionId\":\"debug-session\",\"runId\":\"run1\",\"hypothesisId\":\"E\"}\n");
            fw.close();
        } catch (java.io.IOException ex) {
        }
        // #endregion

        System.out.println("🚀 ========== AIGenerateDescriptionService Initialized ==========");
        System.out.println("🔑 API Key loaded: " + (openAiApiKey != null && !openAiApiKey.trim().isEmpty()));
        if (openAiApiKey != null && !openAiApiKey.trim().isEmpty()) {
            System.out.println("🔑 API Key length: " + openAiApiKey.length());
            System.out.println(
                    "🔑 API Key prefix: " + openAiApiKey.substring(0, Math.min(20, openAiApiKey.length())) + "...");
        } else {
            System.err.println("⚠️ WARNING: OpenAI API key is NOT configured!");
            System.err.println("⚠️ Check application.properties for: ai.openai.api.key");
        }
        System.out.println("🔗 API URL: " + openAiApiUrl);
        System.out.println("================================================================");
    }

    /**
     * Generate product description using AI based on product name
     * 
     * @param productName The name of the product/book
     * @return Generated description in HTML format
     */
    public String generateDescription(String productName) {
        if (productName == null || productName.trim().isEmpty()) {
            return "";
        }

        // Debug: Log API key status
        System.out.println("🔍 ========== AI Generate Description Debug ==========");
        System.out.println("📖 Product Name: " + productName);
        System.out.println("🔑 API Key is null: " + (openAiApiKey == null));
        System.out.println("🔑 API Key is empty: " + (openAiApiKey != null && openAiApiKey.trim().isEmpty()));
        if (openAiApiKey != null && !openAiApiKey.trim().isEmpty()) {
            System.out.println("🔑 API Key length: " + openAiApiKey.length());
            System.out.println(
                    "🔑 API Key starts with: " + openAiApiKey.substring(0, Math.min(10, openAiApiKey.length())));
        }
        System.out.println("🔗 API URL: " + openAiApiUrl);
        System.out.println("=====================================================");

        // If OpenAI API key is not configured, use fallback template-based generation
        if (openAiApiKey == null || openAiApiKey.trim().isEmpty()) {
            System.out.println("⚠️ OpenAI API key not configured, using fallback template");
            return generateFallbackDescription(productName);
        }

        try {
            String description = generateWithOpenAI(productName);
            if (description != null && !description.trim().isEmpty()) {
                return description;
            } else {
                System.out.println("⚠️ Empty description from OpenAI, using fallback");
                return generateFallbackDescription(productName);
            }
        } catch (Exception e) {
            System.err.println("❌ Error calling OpenAI API: " + e.getMessage());
            e.printStackTrace();
            // Fallback to template-based generation
            return generateFallbackDescription(productName);
        }
    }

    /**
     * Generate description using OpenAI API
     */
    private String generateWithOpenAI(String productName) {
        System.out.println("🔍 Calling OpenAI API for book: " + productName);
        System.out.println("🔑 API Key configured: " + (openAiApiKey != null && !openAiApiKey.trim().isEmpty()));
        System.out.println("🔗 API URL: " + openAiApiUrl);

        if (openAiApiKey == null || openAiApiKey.trim().isEmpty()) {
            System.err.println("❌ API Key is null or empty!");
            return generateFallbackDescription(productName);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiApiKey);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-3.5-turbo");

        // Cải thiện prompt để AI thực sự tìm hiểu về cuốn sách
        String prompt = "Bạn là một chuyên gia về sách và văn học. Hãy viết một mô tả chi tiết, hấp dẫn và chuyên nghiệp về cuốn sách có tên: \""
                + productName + "\".\n\n" +
                "Yêu cầu:\n" +
                "1. Nếu bạn biết về cuốn sách này, hãy viết mô tả dựa trên kiến thức thực tế về nội dung, tác giả, thể loại, và ý nghĩa của cuốn sách.\n"
                +
                "2. Nếu bạn không chắc chắn về cuốn sách cụ thể, hãy suy luận dựa trên tên sách và viết mô tả phù hợp với thể loại có thể của nó.\n"
                +
                "3. Mô tả nên bao gồm:\n" +
                "   - Giới thiệu tổng quan về cuốn sách\n" +
                "   - Nội dung chính hoặc cốt truyện (nếu là tiểu thuyết)\n" +
                "   - Thông điệp hoặc bài học từ sách\n" +
                "   - Đối tượng độc giả phù hợp\n" +
                "   - Điểm nổi bật và giá trị của cuốn sách\n" +
                "4. Viết bằng tiếng Việt, tự nhiên và thu hút.\n" +
                "5. Trả về kết quả dưới dạng HTML với các thẻ <h3>, <h4>, <p>, <ul>, <li> để định dạng đẹp.\n" +
                "6. Độ dài khoảng 300-500 từ, đủ chi tiết để người đọc hiểu rõ về cuốn sách.";

        Map<String, Object> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", prompt);

        java.util.List<Map<String, Object>> messagesList = new java.util.ArrayList<>();
        messagesList.add(message);
        requestBody.put("messages", messagesList);
        requestBody.put("max_tokens", 1000); // Tăng lên để có mô tả chi tiết hơn
        requestBody.put("temperature", 0.8); // Tăng creativity một chút

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        try {
            System.out.println("📤 Sending request to OpenAI API...");
            System.out.println("📋 Request model: gpt-3.5-turbo");
            System.out.println("📋 Request max_tokens: 1000");
            @SuppressWarnings("unchecked")
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    openAiApiUrl,
                    HttpMethod.POST,
                    request,
                    (Class<Map<String, Object>>) (Class<?>) Map.class);

            System.out.println("📥 Response status: " + response.getStatusCode());

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();

                // Log error nếu có
                if (responseBody.containsKey("error")) {
                    System.err.println("❌ OpenAI API Error: " + responseBody.get("error"));
                    return generateFallbackDescription(productName);
                }

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
                                        String description = messageMap.get("content").toString();
                                        System.out.println("✅ Successfully generated description from OpenAI (length: "
                                                + description.length() + " chars)");
                                        return description;
                                    }
                                }
                            }
                        }
                    }
                }

                System.err.println("⚠️ Unexpected response format from OpenAI API");
                System.err.println("Response body keys: " + responseBody.keySet());
            } else {
                System.err.println("❌ Invalid response from OpenAI API. Status: " + response.getStatusCode());
            }
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            System.err.println("❌ HTTP Client Error calling OpenAI API");
            System.err.println("   Status Code: " + e.getStatusCode());
            System.err.println("   Status Text: " + e.getStatusText());
            System.err.println("   Message: " + e.getMessage());
            String responseBody = e.getResponseBodyAsString();
            if (responseBody != null) {
                System.err.println("   Response Body: " + responseBody);
            }
            e.printStackTrace();
        } catch (org.springframework.web.client.ResourceAccessException e) {
            System.err.println("❌ Network Error calling OpenAI API: " + e.getMessage());
            System.err.println("   This could be a timeout or connection issue");
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("❌ Unexpected error calling OpenAI API");
            System.err.println("   Error Type: " + e.getClass().getName());
            System.err.println("   Message: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("⚠️ Falling back to template-based description");
        return generateFallbackDescription(productName);
    }

    /**
     * Fallback: Generate description using template when AI API is not available
     */
    private String generateFallbackDescription(String productName) {
        StringBuilder description = new StringBuilder();
        description.append("<h3>Giới thiệu về cuốn sách</h3>");
        description.append("<p><strong>").append(productName)
                .append("</strong> là một cuốn sách đáng đọc với nội dung phong phú và ý nghĩa sâu sắc.</p>");

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

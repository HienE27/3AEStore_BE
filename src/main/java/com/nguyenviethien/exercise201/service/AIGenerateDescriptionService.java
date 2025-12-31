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

    // Google Gemini API Configuration (Free tier available)
    @Value("${GEMINI_API_KEY:${ai.gemini.api.key:}}")
    private String geminiApiKey;

    // Tách base URL và model name để dễ maintain
    @Value("${GEMINI_API_BASE:${ai.gemini.api.base:https://generativelanguage.googleapis.com/v1beta}}")
    private String geminiApiBase;

    @Value("${GEMINI_MODEL:${ai.gemini.api.model:gemini-1.5-flash}}")
    private String geminiModel;

    // Cache cho model đã chọn (để tránh list models mỗi lần)
    private String cachedModelName = null;

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
        System.out.println("🚀 ========== AIGenerateDescriptionService Initialized ==========");
        System.out.println("🟢 Gemini API Key loaded: " + (geminiApiKey != null && !geminiApiKey.trim().isEmpty()));

        if (geminiApiKey != null && !geminiApiKey.trim().isEmpty()) {
            System.out.println("🟢 Gemini API Key length: " + geminiApiKey.length());
            System.out.println("🟢 Gemini API Key prefix: "
                    + geminiApiKey.substring(0, Math.min(15, geminiApiKey.length())) + "...");
            System.out.println("🔗 API Base: " + geminiApiBase);
            System.out.println("🤖 Model: " + geminiModel);

            // Tự động detect và chọn model tốt nhất
            try {
                String detectedModel = detectBestModel();
                if (detectedModel != null && !detectedModel.equals(geminiModel)) {
                    System.out.println("✨ Auto-detected better model: " + detectedModel);
                    cachedModelName = detectedModel;
                } else {
                    cachedModelName = geminiModel;
                }
            } catch (Exception e) {
                System.out.println("⚠️ Could not auto-detect model, using configured: " + geminiModel);
                cachedModelName = geminiModel;
            }

            System.out.println("✅ AI description generation ENABLED (using Gemini)");
        } else {
            System.err.println("⚠️ WARNING: Gemini API key NOT configured!");
            System.err.println("⚠️ Get free API key: https://makersuite.google.com/app/apikey");
            System.err.println("⚠️ Will use fallback template-based generation");
        }

        System.out.println("================================================================");
    }

    /**
     * Tự động detect và chọn model tốt nhất từ danh sách models available
     * Ưu tiên: gemini-2.0-flash > gemini-1.5-flash > gemini-pro > bất kỳ flash
     * model nào
     */
    private String detectBestModel() {
        try {
            String listModelsUrl = geminiApiBase + "/models?key=" + geminiApiKey;
            System.out.println("🔍 Auto-detecting best Gemini model...");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<?> request = new HttpEntity<>(headers);

            @SuppressWarnings("unchecked")
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    listModelsUrl,
                    HttpMethod.GET,
                    request,
                    (Class<Map<String, Object>>) (Class<?>) Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();

                if (responseBody.containsKey("models")) {
                    Object modelsObj = responseBody.get("models");
                    if (modelsObj instanceof java.util.List) {
                        @SuppressWarnings("unchecked")
                        java.util.List<Map<String, Object>> models = (java.util.List<Map<String, Object>>) modelsObj;

                        // Ưu tiên các model theo thứ tự (tránh experimental models: -exp, -beta)
                        // Experimental models thường không có free tier quota
                        String[] preferredModels = {
                                "gemini-2.0-flash",      // Stable version
                                "gemini-1.5-flash",      // Stable version
                                "gemini-pro",            // Stable version
                                "gemini-1.5-flash-latest" // Latest stable
                        };

                        // Tìm model tốt nhất (ưu tiên stable, tránh experimental)
                        for (String preferred : preferredModels) {
                            for (Map<String, Object> model : models) {
                                String modelName = (String) model.get("name");
                                if (modelName != null && modelName.contains(preferred)) {
                                    // Bỏ qua experimental models (không có free tier quota)
                                    if (modelName.contains("-exp") || modelName.contains("-beta") || 
                                        modelName.contains("experimental")) {
                                        continue;
                                    }
                                    
                                    // Kiểm tra xem model có support generateContent không
                                    Object supportedMethods = model.get("supportedGenerationMethods");
                                    if (supportedMethods instanceof java.util.List) {
                                        @SuppressWarnings("unchecked")
                                        java.util.List<String> methods = (java.util.List<String>) supportedMethods;
                                        if (methods.contains("generateContent")) {
                                            // Extract model name từ full path (vd: models/gemini-1.5-flash)
                                            String[] parts = modelName.split("/");
                                            if (parts.length > 0) {
                                                String extractedModel = parts[parts.length - 1];
                                                System.out.println("✅ Found suitable model: " + extractedModel);
                                                return extractedModel;
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Nếu không tìm thấy preferred, tìm bất kỳ flash model nào (tránh experimental)
                        for (Map<String, Object> model : models) {
                            String modelName = (String) model.get("name");
                            if (modelName != null && modelName.contains("flash")) {
                                // Bỏ qua experimental models (không có free tier quota)
                                if (modelName.contains("-exp") || modelName.contains("-beta") || 
                                    modelName.contains("experimental")) {
                                    continue;
                                }
                                
                                Object supportedMethods = model.get("supportedGenerationMethods");
                                if (supportedMethods instanceof java.util.List) {
                                    @SuppressWarnings("unchecked")
                                    java.util.List<String> methods = (java.util.List<String>) supportedMethods;
                                    if (methods.contains("generateContent")) {
                                        String[] parts = modelName.split("/");
                                        if (parts.length > 0) {
                                            String extractedModel = parts[parts.length - 1];
                                            System.out.println("✅ Found flash model: " + extractedModel);
                                            return extractedModel;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("⚠️ Could not list models: " + e.getMessage());
        }

        // Fallback về model đã config
        return geminiModel;
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

        System.out.println("🔍 ========== AI Generate Description ==========");
        System.out.println("📖 Product Name: " + productName);

        // Try Gemini API
        if (geminiApiKey != null && !geminiApiKey.trim().isEmpty()) {
            System.out.println("🟢 Using Google Gemini API");
            try {
                String description = generateWithGemini(productName);
                if (description != null && !description.trim().isEmpty()) {
                    System.out.println("✅ Success with Gemini API");
                    return description;
                }
            } catch (Exception e) {
                System.err.println("⚠️ Gemini API failed: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("⚠️ Gemini API key not configured");
            System.out.println("💡 Get free API key: https://makersuite.google.com/app/apikey");
        }

        // Fallback: Template-based generation
        System.out.println("⚠️ Using template-based generation");
        return generateFallbackDescription(productName);
    }

    /**
     * Generate description using Google Gemini API (Primary - Free tier)
     */
    private String generateWithGemini(String productName) {
        System.out.println("🟢 Calling Google Gemini API for book: " + productName);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Tự động ghép URL từ base + model (không cần hardcode full URL)
        String modelToUse = cachedModelName != null ? cachedModelName : geminiModel;
        String apiUrl = geminiApiBase + "/models/" + modelToUse + ":generateContent?key=" + geminiApiKey;
        System.out.println("🔗 Using endpoint: " + apiUrl.replace(geminiApiKey, "***"));

        // Tạo prompt cho Gemini
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

        // Gemini API request format
        Map<String, Object> requestBody = new HashMap<>();

        Map<String, Object> part = new HashMap<>();
        part.put("text", prompt);

        Map<String, Object> content = new HashMap<>();
        content.put("parts", new Object[] { part });

        requestBody.put("contents", new Object[] { content });

        // Generation config
        Map<String, Object> generationConfig = new HashMap<>();
        generationConfig.put("temperature", 0.8);
        generationConfig.put("maxOutputTokens", 2000);
        requestBody.put("generationConfig", generationConfig);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        try {
            System.out.println("📤 Sending request to Gemini API...");

            @SuppressWarnings("unchecked")
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    apiUrl,
                    HttpMethod.POST,
                    request,
                    (Class<Map<String, Object>>) (Class<?>) Map.class);

            System.out.println("📥 Response status: " + response.getStatusCode());

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();

                // Parse Gemini response format
                if (responseBody.containsKey("candidates")) {
                    Object candidatesObj = responseBody.get("candidates");
                    if (candidatesObj instanceof java.util.List && !((java.util.List<?>) candidatesObj).isEmpty()) {
                        Object firstCandidate = ((java.util.List<?>) candidatesObj).get(0);
                        if (firstCandidate instanceof Map) {
                            @SuppressWarnings("unchecked")
                            Map<String, Object> candidate = (Map<String, Object>) firstCandidate;
                            if (candidate.containsKey("content")) {
                                Object contentObj = candidate.get("content");
                                if (contentObj instanceof Map) {
                                    @SuppressWarnings("unchecked")
                                    Map<String, Object> contentMap = (Map<String, Object>) contentObj;
                                    if (contentMap.containsKey("parts")) {
                                        Object partsObj = contentMap.get("parts");
                                        if (partsObj instanceof java.util.List
                                                && !((java.util.List<?>) partsObj).isEmpty()) {
                                            Object firstPart = ((java.util.List<?>) partsObj).get(0);
                                            if (firstPart instanceof Map) {
                                                @SuppressWarnings("unchecked")
                                                Map<String, Object> partMap = (Map<String, Object>) firstPart;
                                                if (partMap.containsKey("text")) {
                                                    String description = partMap.get("text").toString();
                                                    System.out.println(
                                                            "✅ Successfully generated description from Gemini (length: "
                                                                    + description.length() + " chars)");
                                                    return description;
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                System.err.println("⚠️ Unexpected response format from Gemini API");
                System.err.println("Response body keys: " + responseBody.keySet());
            }
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            System.err.println("❌ HTTP Error calling Gemini API");
            System.err.println("   Status: " + e.getStatusCode());
            System.err.println("   Message: " + e.getMessage());
            
            // Nếu gặp 429 (quota exceeded) với experimental model, log warning
            if (e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                String currentModel = cachedModelName != null ? cachedModelName : geminiModel;
                if (currentModel.contains("-exp") || currentModel.contains("-beta")) {
                    System.err.println("⚠️ WARNING: Experimental model '" + currentModel + "' has no free tier quota!");
                    System.err.println("💡 Tip: Auto-detection will skip experimental models on next restart");
                }
            }
            
            if (e.getResponseBodyAsString() != null) {
                System.err.println("   Response: " + e.getResponseBodyAsString());
            }
        } catch (Exception e) {
            System.err.println("❌ Error calling Gemini API: " + e.getMessage());
            e.printStackTrace();
        }

        return null; // Return null to try next provider
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

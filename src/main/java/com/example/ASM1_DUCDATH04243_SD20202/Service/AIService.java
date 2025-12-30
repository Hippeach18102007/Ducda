package com.example.ASM1_DUCDATH04243_SD20202.Service;

import com.example.ASM1_DUCDATH04243_SD20202.Model.StudentManager;
import com.example.ASM1_DUCDATH04243_SD20202.Respository.StudentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.concurrent.CompletableFuture;
import java.util.*;

@Service
public class AIService {

    @Value("${openrouter.api.key:default_key}")
    private String apiKey;

    private final RestTemplate restTemplate;
    private final StudentRepository studentRepository; // Inject StudentRepository

    // Sửa constructor để Spring tự động inject StudentRepository
    public AIService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
        this.restTemplate = createRestTemplateWithTimeout();
    }

    private RestTemplate createRestTemplateWithTimeout() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(30000);
        requestFactory.setReadTimeout(30000);
        return new RestTemplate(requestFactory);
    }

    // Sửa hàm getChatResponse để nhận thêm studentId
    @Async
    public CompletableFuture<String> getChatResponse(String userPrompt, Integer studentId) {
        if (apiKey == null || apiKey.equals("default_key") || apiKey.isEmpty()) {
            return CompletableFuture.completedFuture(generateSmartResponse(userPrompt));
        }

        // BƯỚC 1: LẤY DỮ LIỆU SINH VIÊN TỪ REPOSITORY
        StudentManager student = studentRepository.findById(studentId);
        if (student == null) {
            return CompletableFuture.completedFuture("Lỗi: Không tìm thấy sinh viên với ID " + studentId);
        }

        // BƯỚC 2: TẠO CHUỖI "BỐI CẢNH" (CONTEXT)
        String studentContext = String.format(
                "Bạn là trợ lý AI cho một hệ thống quản lý sinh viên. Hãy dựa vào thông tin của sinh viên đang hỏi dưới đây để trả lời câu hỏi của họ một cách cá nhân hóa và chuyên nghiệp.\n\n" +
                        "--- Thông tin sinh viên ---\n" +
                        "- Tên: %s\n" +
                        "- Mã SV: %d\n" +
                        "- Chuyên ngành: %s\n" +
                        "- Email: %s\n" +
                        "- SĐT: %s\n" +
                        "---------------------------\n\n",
                student.getStudentName(),
                student.getId(),
                student.getChuyenNganh(),
                student.getEmail(),
                student.getSdt()
        );

        // BƯỚC 3: XÂY DỰNG PROMPT HOÀN CHỈNH
        String finalPrompt = studentContext + "Câu hỏi của sinh viên: " + userPrompt;

        // --- PHẦN GỌI API ---
        String apiUrl = "https://openrouter.ai/api/v1/chat/completions";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);
        headers.set("HTTP-Referer", "http://localhost:9000");
        headers.set("X-Title", "Student Management System");

        String[] availableModels = {
                "openchat/openchat-7b:free",                  // Mạnh hơn Mistral 7B gốc
                "undi95/toppy-m-7b:free",
                "meta-llama/llama-3-8b-instruct",             // Llama 3 mới nhất, trả lời chất lượng cao

                // === Lựa chọn thay thế chất lượng ===
                                    // Một model cộng đồng được đánh giá cao

                // === Lựa chọn dự phòng (nhanh nhưng có thể trả lời chung chung) ===
                "microsoft/phi-3-mini-128k-instruct"
        };

        for (String model : availableModels) {
            try {
                Map<String, Object> requestBody = new HashMap<>();
                requestBody.put("model", model);
                List<Map<String, String>> messages = new ArrayList<>();
                Map<String, String> message = new HashMap<>();
                message.put("role", "user");
                // Sử dụng prompt hoàn chỉnh đã có bối cảnh
                message.put("content", finalPrompt);
                messages.add(message);

                requestBody.put("messages", messages);
                requestBody.put("max_tokens", 1024);

                HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
                Map response = restTemplate.postForObject(apiUrl, entity, Map.class);

                if (response != null && response.containsKey("choices")) {
                    List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
                    if (choices != null && !choices.isEmpty()) {
                        Map<String, Object> messageResponse = (Map<String, Object>) choices.get(0).get("message");
                        String aiResponse = (String) messageResponse.get("content");
                        return CompletableFuture.completedFuture(aiResponse.trim());
                    }
                }
            } catch (Exception e) {
                System.err.println("Lỗi với model " + model + ": " + e.getMessage());
                // Tiếp tục thử model tiếp theo
            }
        }

        return CompletableFuture.completedFuture(generateSmartResponse(userPrompt));
    }

    private String generateSmartResponse(String userPrompt) {
        String lowerPrompt = userPrompt.toLowerCase();

        // Phân tích keyword và trả lời phù hợp
        if (lowerPrompt.contains("xin chào") || lowerPrompt.contains("hello") || lowerPrompt.contains("hi")) {
            return "Xin chào! Tôi là trợ lý AI của hệ thống quản lý sinh viên. Tôi có thể giúp gì cho bạn về học tập, điểm số, thời khóa biểu hoặc thông tin sinh viên?";
        } else if (lowerPrompt.contains("điểm") || lowerPrompt.contains("kết quả") || lowerPrompt.contains("bảng điểm")) {
            return "Để tra cứu điểm:\n\n📊 1. Đăng nhập vào hệ thống\n📊 2. Truy cập mục 'Kết quả học tập' \n📊 3. Chọn học kỳ cần xem\n\n❓ Nếu có vấn đề về điểm số, hãy liên hệ phòng Đào tạo.";
        } else if (lowerPrompt.contains("học") || lowerPrompt.contains("học tập") || lowerPrompt.contains("môn học")) {
            return "Về vấn đề học tập:\n\n📚 Lịch học: Xem trên portal sinh viên\n📚 Tài liệu: Truy cập thư viện số\n📚 Lịch thi: Kiểm tra mục 'Thông báo'\n📚 Hỗ trợ: Liên hệ giảng viên bộ môn";
        } else if (lowerPrompt.contains("thời khóa biểu") || lowerPrompt.contains("lịch học") || lowerPrompt.contains("lịch trình")) {
            return "Thời khóa biểu được cập nhật tại:\n\n🗓️ Portal sinh viên\n🗓️ Ứng dụng di động của trường\n🗓️ Bảng thông tin tại các khoa\n\n⚠️ Lịch học có thể thay đổi, vui lòng kiểm tra thường xuyên.";
        } else if (lowerPrompt.contains("học phí") || lowerPrompt.contains("tài chính") || lowerPrompt.contains("phí")) {
            return "Thông tin học phí:\n\n💳 Xem chi tiết trên portal cá nhân\n💳 Liên hệ phòng Tài chính\n💳 Email: taichinh@university.edu.vn\n💳 Giờ làm việc: 8h00-17h00 (Thứ 2 - Thứ 6)";
        } else if (lowerPrompt.contains("cảm ơn") || lowerPrompt.contains("thanks") || lowerPrompt.contains("thank you")) {
            return "Không có gì! Rất vui được hỗ trợ bạn. Chúc bạn học tập thật tốt! 🎓";
        } else if (lowerPrompt.contains("tạm biệt") || lowerPrompt.contains("goodbye") || lowerPrompt.contains("bye")) {
            return "Tạm biệt! Nếu có thắc mắc gì thêm, đừng ngần ngại hỏi tôi nhé! ✨";
        } else if (lowerPrompt.contains("là ai") || lowerPrompt.contains("bạn là ai")) {
            return "Tôi là trợ lý AI thông minh của hệ thống quản lý sinh viên. Tôi có thể hỗ trợ bạn tra cứu thông tin học tập, điểm số, lịch học và các vấn đề liên quan đến sinh viên.";
        } else {
            return "Tôi hiểu câu hỏi của bạn. Hiện tại tôi có thể hỗ trợ các vấn đề về:\n\n📚 Học tập và điểm số\n🗓️ Thời khóa biểu\n💳 Học phí\n👨‍🎓 Thông tin sinh viên\n\nBạn có thể hỏi cụ thể hơn hoặc liên hệ phòng Đào tạo để được hỗ trợ trực tiếp.";
        }
    }
    @Async
    public CompletableFuture<String> getSystemChatResponse(String userPrompt, String context) {
        if (apiKey == null || apiKey.equals("default_key") || apiKey.isEmpty()) {
            return CompletableFuture.completedFuture(generateSmartResponse(userPrompt));
        }

        // Xây dựng prompt với bối cảnh được gửi từ frontend
        String finalPrompt = "Bạn là trợ lý AI cho hệ thống quản lý sinh viên. Dưới đây là bản tóm tắt dữ liệu toàn hệ thống:\n\n"
                + context
                + "\n\nHãy dựa vào bản tóm tắt trên để trả lời câu hỏi của người dùng một cách chính xác.\n\nCâu hỏi: " + userPrompt;

        // ... (Phần code gọi API OpenRouter giữ nguyên y hệt hàm getChatResponse cũ) ...
        // ... Chỉ cần đảm bảo bạn sử dụng "finalPrompt" ở trên cho message.put("content", finalPrompt);

        // (Copy và paste phần vòng lặp for gọi API từ hàm getChatResponse cũ vào đây)
        // ...

        return CompletableFuture.completedFuture(generateSmartResponse(userPrompt)); // Fallback
    }
}
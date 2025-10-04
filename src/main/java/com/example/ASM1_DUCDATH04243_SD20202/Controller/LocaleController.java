package com.example.ASM1_DUCDATH04243_SD20202.Controller;




import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LocaleController {

    /**
     * Endpoint chung để chuyển đổi ngôn ngữ và quay lại trang trước đó.
     * LocaleChangeInterceptor sẽ tự động xử lý việc thay đổi Locale.
     * @param lang Ngôn ngữ muốn chuyển đổi (ví dụ: 'vi', 'en').
     * @param request HttpServletRequest để lấy URL của trang trước đó (Referer).
     * @return String: Lệnh chuyển hướng (redirect) về trang trước.
     */
    @GetMapping("/change-locale")
    public String changeLocale(@RequestParam("lang") String lang, HttpServletRequest request) {
        // LocaleChangeInterceptor đã xử lý việc lưu trữ Locale mới (ví dụ: trong Cookie)

        // Lấy URL của trang trước đó từ HTTP Referer header
        String referer = request.getHeader("Referer");

        // Chuyển hướng người dùng về trang trước (hoặc trang chủ nếu Referer null)
        if (referer != null && !referer.isEmpty()) {
            return "redirect:" + referer;
        } else {
            // Trường hợp không có Referer, chuyển về trang danh sách sinh viên mặc định
            return "redirect:/students";
        }
    }
}

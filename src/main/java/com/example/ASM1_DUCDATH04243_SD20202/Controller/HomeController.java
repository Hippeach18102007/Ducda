package com.example.ASM1_DUCDATH04243_SD20202.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        // Khi người dùng truy cập trang chủ, chuyển hướng họ tới /students/list
        return "redirect:/students/list";
    }
}
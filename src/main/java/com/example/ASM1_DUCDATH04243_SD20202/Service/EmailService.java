package com.example.ASM1_DUCDATH04243_SD20202.Service;

import com.example.ASM1_DUCDATH04243_SD20202.Model.ClassManager;
import com.example.ASM1_DUCDATH04243_SD20202.Model.StudentManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Async // Chạy bất đồng bộ để không làm chậm request chính khi gửi email
    public void sendWelcomeEmail(StudentManager student, ClassManager classInfo) {
        if (student.getEmail() == null || student.getEmail().isEmpty()) {
            System.err.println("Không thể gửi email: Sinh viên " + student.getStudentName() + " không có địa chỉ email.");
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(student.getEmail());
            message.setSubject("Chào mừng bạn đã được xếp lớp!");

            String text = String.format(
                    "Xin chào %s,\n\n" +
                            "Chúc mừng bạn đã được xếp vào lớp học mới trong hệ thống của chúng tôi.\n\n" +
                            "Thông tin chi tiết:\n" +
                            "- Lớp: %s\n" +
                            "- Chuyên ngành: %s\n\n" +
                            "Chúc bạn có một kỳ học thành công!\n\n" +
                            "Trân trọng,\n" +
                            "Hệ thống Quản lý Sinh viên",
                    student.getStudentName(),
                    classInfo.getClassName(),
                    classInfo.getKhoaHoc()
            );
            message.setText(text);

            mailSender.send(message);
            System.out.println("Đã gửi email chào mừng tới: " + student.getEmail());
        } catch (Exception e) {
            System.err.println("Lỗi khi gửi email tới " + student.getEmail() + ": " + e.getMessage());
        }
    }
}

package com.example.ASM1_DUCDATH04243_SD20202.Controller;

import com.example.ASM1_DUCDATH04243_SD20202.Model.ClassManager;
import com.example.ASM1_DUCDATH04243_SD20202.Model.DiemManager;
import com.example.ASM1_DUCDATH04243_SD20202.Service.DiemService;
import com.example.ASM1_DUCDATH04243_SD20202.Respository.StudentRepository; // Cần để lấy danh sách sinh viên
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Set;

@Controller
@RequestMapping("/diems")
public class DiemController {

    @Autowired
    private DiemService diemService;

    @Autowired
    private StudentRepository studentRepository; // Cần thiết

    /** Hiển thị form thêm điểm */
    @GetMapping("/add")
    public String showAddDiemForm(Model model) {
        // 1. Lấy dữ liệu cần thiết cho các bộ lọc từ Service
        Set<String> uniqueMajors = diemService.getAllUniqueMajors();
        List<ClassManager> allClasses = diemService.getAllClasses();

        // 2. Thêm tất cả dữ liệu vào Model để gửi ra View
        model.addAttribute("diemManager", new DiemManager());
        model.addAttribute("allStudents", studentRepository.findAll()); // Gửi toàn bộ sinh viên
        model.addAttribute("majors", uniqueMajors); // Gửi danh sách chuyên ngành
        model.addAttribute("classes", allClasses); // Gửi danh sách lớp học

        return "diem-add"; // Tên file
    }
    /** Xử lý Submit Form Thêm Điểm */
    @PostMapping("/add")
    public String addDiem(@ModelAttribute DiemManager diemManager, RedirectAttributes redirectAttributes) {
        diemService.saveNewDiem(diemManager);
        redirectAttributes.addFlashAttribute("message", "Thêm điểm thành công!");
        return "redirect:/students/list";
    }

    // -------------------------------------------------------------
    // CHỨC NĂNG SỬA ĐIỂM (KHẮC PHỤC LỖI 404)
    // -------------------------------------------------------------

    /** Hiển thị form sửa thông tin điểm. */
    @GetMapping("/edit/{id}")
    public String showEditDiemForm(@PathVariable Integer id, Model model) {
        DiemManager diemManager = diemService.getDiemById(id);

        if (diemManager == null) {
            // Chuyển hướng nếu không tìm thấy ID điểm
            return "redirect:/students/list";
        }

        model.addAttribute("diemManager", diemManager);
        // Truyền danh sách sinh viên để hiển thị tên sinh viên
        model.addAttribute("students", studentRepository.findAll());

        return "diem-edit";
    }

    // --- XỬ LÝ SUBMIT CẬP NHẬT (POST) ---
    @PostMapping("/edit")
    public String editDiem(@ModelAttribute DiemManager diemManager, RedirectAttributes redirectAttributes) {
        try {
            diemService.updateDiem(diemManager);
            redirectAttributes.addFlashAttribute("message", "Cập nhật điểm thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi cập nhật điểm: " + e.getMessage());
            // Trở lại trang sửa nếu có lỗi
            return "redirect:/diems/edit/" + diemManager.getDiemid();
        }

        // Chuyển hướng về trang chi tiết của sinh viên sau khi sửa thành công
        return "redirect:/students/detail/" + diemManager.getId();
    }

    @GetMapping("/delete/{diemId}") // CHỈ CẦN DIEM ID
    public String deleteDiem(@PathVariable("diemId") Integer diemId, RedirectAttributes redirectAttributes) {
        Integer studentId = null;
        try {
            // Gọi service để xóa và lấy studentId
            studentId = diemService.deleteDiem(diemId);

            if (studentId == null) {
                // Trường hợp không tìm thấy điểm (đã được xử lý trong service)
                redirectAttributes.addFlashAttribute("errorMessage", "❌ Xóa điểm thất bại: Không tìm thấy bản ghi điểm.");
                return "redirect:/students/list";
            }

            redirectAttributes.addFlashAttribute("message", "✅ Xóa điểm thành công!");

            // Redirect về trang chi tiết sinh viên
            return "redirect:/students/detail/" + studentId;

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "❌ Thao tác thất bại: " + e.getMessage());
            return "redirect:/students/list"; // Hoặc một trang lỗi khác
        } catch (Exception e) {
            // Trường hợp lỗi khác (ví dụ: lỗi database)
            redirectAttributes.addFlashAttribute("errorMessage", "❌ Xóa điểm thất bại. Lỗi hệ thống.");
            return "redirect:/students/detail/" + (studentId != null ? studentId : "list");
        }
    }
    }

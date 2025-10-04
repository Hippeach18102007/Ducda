package com.example.ASM1_DUCDATH04243_SD20202.Controller;


import com.example.ASM1_DUCDATH04243_SD20202.Model.ClassManager;
import com.example.ASM1_DUCDATH04243_SD20202.Model.StudentManager;
import com.example.ASM1_DUCDATH04243_SD20202.Respository.LopHocRepository;
import com.example.ASM1_DUCDATH04243_SD20202.Service.ClassService;
import com.example.ASM1_DUCDATH04243_SD20202.Service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/classes")
public class ClassController {

    @Autowired
    private ClassService classService;
    @Autowired
    private LopHocRepository lopHocRepository;
    @Autowired
    private StudentService studentService;

    @GetMapping("/add")
    public String showAddForm(Model model) {
        // 1. Đối tượng ClassManager rỗng cho form
        model.addAttribute("classManager", new ClassManager());

        // 2. THÊM DANH SÁCH LỚP CHO BẢNG HIỂN THỊ
        model.addAttribute("classes", lopHocRepository.findAll());

        // 3. LOGIC MỚI: THÊM DANH SÁCH SINH VIÊN CHƯA XẾP LỚP (NHÓM THEO CHUYÊN NGÀNH)
        Map<String, List<StudentManager>> groupedStudents = studentService.getUnassignedStudentsGroupedByMajor();
        model.addAttribute("groupedStudents", groupedStudents);
        model.addAttribute("majors", groupedStudents.keySet());


        return "class-add";
    }

    // Xử lý Submit Form Thêm Lớp học
    @PostMapping("/add")
    public String addClass(@ModelAttribute ClassManager classManager, RedirectAttributes redirectAttributes) {
        try {
            classService.saveNewClass(classManager);
            // Cập nhật: Chuyển hướng người dùng về chính trang classes/add
            redirectAttributes.addFlashAttribute("message", "Đã thêm lớp học '" + classManager.getClassName() + "' thành công!");
            return "redirect:/classes/add"; // <-- ĐÃ SỬA
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi lưu lớp học: " + e.getMessage());
            return "redirect:/classes/add";
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditClassForm(@PathVariable Integer id, Model model) {
        ClassManager classManager = classService.getClassById(id);

        if (classManager == null) {
            return "redirect:/students/list";
        }

        model.addAttribute("classManager", classManager);
        return "class-edit";
    }

    @PostMapping("/edit") // Không có {id} ở đây
    public String editClass(@ModelAttribute ClassManager classManager, RedirectAttributes redirectAttributes) {
        try {
            classService.updateClass(classManager);
            redirectAttributes.addFlashAttribute("message", "Cập nhật lớp học thành công!");
            return "redirect:/students/list"; // Giữ nguyên, quay về trang danh sách sinh viên
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi cập nhật lớp học: " + e.getMessage());
            // Quay về trang edit nếu có lỗi (hoặc về list, tùy theo quy tắc ứng dụng)
            return "redirect:/classes/edit/" + classManager.getIdClass();
        }
    }
}
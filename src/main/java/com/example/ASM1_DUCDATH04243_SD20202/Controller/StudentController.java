package com.example.ASM1_DUCDATH04243_SD20202.Controller;

import com.example.ASM1_DUCDATH04243_SD20202.Model.StudentDetailDTO;
import com.example.ASM1_DUCDATH04243_SD20202.Model.StudentManager;
import com.example.ASM1_DUCDATH04243_SD20202.Respository.LopHocRepository;
import com.example.ASM1_DUCDATH04243_SD20202.Service.DiemService;
import com.example.ASM1_DUCDATH04243_SD20202.Service.ExcelExportService;
import com.example.ASM1_DUCDATH04243_SD20202.Service.StudentService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/students")
public class StudentController {

    @Autowired private StudentService studentService;
    @Autowired private ExcelExportService excelExportService;
    @Autowired private DiemService diemService;
    @Autowired private LopHocRepository lopHocRepository;


    @GetMapping("/list")
    public String showStudentList(Model model) {
        model.addAttribute("students", studentService.getAllStudentDetails());
        model.addAttribute("academicChartData", studentService.calculateOverallAcademicDistribution());
        model.addAttribute("majorChartData", studentService.calculateMajorDistribution());
        model.addAttribute("allMajors", diemService.getAllUniqueMajors());
        model.addAttribute("allClasses", diemService.getAllClasses());
        return "student-list";
    }

    @PostMapping("/upload-excel")
    public String uploadExcelFile(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {
        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng chọn một file để tải lên.");
            return "redirect:/students/list";
        }
        try {
            // Nhận kết quả chi tiết từ service
            Map<String, Object> result = studentService.importStudentsFromExcel(file);
            int successCount = (int) result.get("successCount");
            int failureCount = (int) result.get("failureCount");
            List<String> errors = (List<String>) result.get("errors");

            // Tạo thông báo dựa trên kết quả
            if (failureCount == 0 && successCount > 0) {
                redirectAttributes.addFlashAttribute("message", "Nhập thành công " + successCount + " sinh viên từ file Excel.");
            } else {
                String summaryMessage = "Hoàn tất import: " + successCount + " thành công, " + failureCount + " thất bại.";
                redirectAttributes.addFlashAttribute("errorMessage", summaryMessage);
                if (!errors.isEmpty()) {
                    redirectAttributes.addFlashAttribute("importErrors", errors);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi nghiêm trọng khi đọc file: " + e.getMessage());
        }
        return "redirect:/students/list";
    }

    // ======================================================================
    // CÁC HÀM CRUD VÀ CHỨC NĂNG KHÁC (GIỮ NGUYÊN)
    // ======================================================================

    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("student", new StudentManager());
        model.addAttribute("classes", lopHocRepository.findAll());
        return "student-add";
    }

    @PostMapping("/add")
    public String addStudent(@ModelAttribute StudentManager student, RedirectAttributes redirectAttributes) {
        try {
            studentService.saveNewStudent(student);
            redirectAttributes.addFlashAttribute("message", "Thêm sinh viên thành công!");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            redirectAttributes.addFlashAttribute("student", student);
            return "redirect:/students/add";
        }
        return "redirect:/students/list";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Integer id, Model model) {
        StudentManager student = studentService.getStudentById(id);
        if (student == null) {
            return "redirect:/students/list";
        }
        model.addAttribute("student", student);
        model.addAttribute("classes", lopHocRepository.findAll());
        return "student-edit";
    }

    @PostMapping("/edit")
    public String editStudent(@ModelAttribute StudentManager student, RedirectAttributes redirectAttributes) {
        try {
            studentService.updateStudent(student);
            redirectAttributes.addFlashAttribute("message", "Cập nhật sinh viên thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi cập nhật: " + e.getMessage());
            return "redirect:/students/edit/" + student.getId();
        }
        return "redirect:/students/list";
    }

    @GetMapping("/delete/{id}")
    public String deleteStudent(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        studentService.deleteStudent(id);
        redirectAttributes.addFlashAttribute("message", "Đã xóa sinh viên ID " + id + " thành công.");
        return "redirect:/students/list";
    }

    @GetMapping("/detail/{id}")
    public String showStudentDetail(@PathVariable Integer id, Model model) {
        StudentDetailDTO studentDetail = studentService.getStudentDetailsById(id);
        if (studentDetail == null) {
            return "redirect:/students/list";
        }
        Map<String, Object> academicSummary = studentService.calculateAcademicSummary(studentDetail);
        model.addAttribute("summary", academicSummary);
        model.addAttribute("student", studentDetail);
        return "student-detail";
    }

    @PostMapping("/assign-class/{id}")
    public String assignClassAutomatically(@PathVariable("id") Integer studentId, RedirectAttributes redirectAttributes) {
        try {
            String resultMessage = studentService.autoAssignClassForUnassignedStudent(studentId);
            if (resultMessage.startsWith("Đã xếp")) {
                redirectAttributes.addFlashAttribute("message", resultMessage);
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", resultMessage);
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi hệ thống khi xếp lớp: " + e.getMessage());
        }
        return "redirect:/classes/add";
    }

    @GetMapping("/export-excel")
    public void exportToExcel(HttpServletResponse response) throws IOException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        String currentDateTime = dateFormatter.format(new Date());
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=BaoCao_SinhVien_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        byte[] excelBytes = excelExportService.exportStudentDetailsToExcel();
        response.getOutputStream().write(excelBytes);
    }
}

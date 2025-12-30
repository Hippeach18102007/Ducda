package com.example.ASM1_DUCDATH04243_SD20202.Controller;

import com.example.ASM1_DUCDATH04243_SD20202.Model.StudentDetailDTO;
import com.example.ASM1_DUCDATH04243_SD20202.Model.StudentManager;
import com.example.ASM1_DUCDATH04243_SD20202.Respository.LopHocRepository;
import com.example.ASM1_DUCDATH04243_SD20202.Respository.StudentRepository; // Thêm import này
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

    // 👇 THÊM DÒNG NÀY ĐỂ TRUY CẬP DANH SÁCH SINH VIÊN GỐC 👇
    @Autowired private StudentRepository studentRepository;


    @GetMapping("/list")
    public String showStudentList(Model model) {
        // --- Phần code cũ của bạn (giữ nguyên) ---
        model.addAttribute("students", studentService.getAllStudentDetails());
        Map<String, Object> academicChartData = studentService.calculateOverallAcademicDistribution();
        Map<String, Object> majorChartData = studentService.calculateMajorDistribution();
        model.addAttribute("academicChartData", academicChartData);
        model.addAttribute("majorChartData", majorChartData);
        model.addAttribute("allMajors", diemService.getAllUniqueMajors());
        model.addAttribute("allClasses", diemService.getAllClasses());

        // ----- 👇 PHẦN CẬP NHẬT CHO AI CONTEXT BẮT ĐẦU TẠI ĐÂY 👇 -----

        // 1. Tạo chuỗi tóm tắt dữ liệu sinh viên
        List<StudentManager> allStudents = studentRepository.findAll();
        StringBuilder studentSummary = new StringBuilder();
        studentSummary.append("Hệ thống hiện có ").append(allStudents.size()).append(" sinh viên.\n");
        studentSummary.append("Thông tin chi tiết một vài sinh viên (tối đa 5):\n");
        allStudents.stream().limit(5).forEach(sv ->
                studentSummary.append(String.format("- Tên: %s, Email: %s, Chuyên ngành: %s\n",
                        sv.getStudentName(), sv.getEmail(), sv.getChuyenNganh()))
        );

        // 2. Tạo chuỗi tóm tắt dữ liệu biểu đồ
        StringBuilder chartSummary = new StringBuilder();
        chartSummary.append("Dữ liệu biểu đồ học lực:\n");
        if (academicChartData != null && academicChartData.containsKey("counts")) {
            try {
                Map<String, Long> counts = (Map<String, Long>) academicChartData.get("counts");
                counts.forEach((status, count) ->
                        chartSummary.append(String.format("- Xếp loại %s: %d sinh viên\n", status, count))
                );
            } catch (Exception e) {
                // Bỏ qua nếu cast lỗi
            }
        }
        chartSummary.append("\nDữ liệu biểu đồ chuyên ngành:\n");
        if (majorChartData != null && majorChartData.containsKey("majorCounts")) {
            try {
                Map<String, Long> majorCounts = (Map<String, Long>) majorChartData.get("majorCounts");
                majorCounts.forEach((major, count) ->
                        chartSummary.append(String.format("- Chuyên ngành %s: %d sinh viên\n", major, count))
                );
            } catch (Exception e) {
                // Bỏ qua nếu cast lỗi
            }
        }

        // 3. Đưa các chuỗi tóm tắt vào Model
        model.addAttribute("studentDataSummary", studentSummary.toString());
        model.addAttribute("chartDataSummary", chartSummary.toString());

        // ----- KẾT THÚC PHẦN CẬP NHẬT -----

        return "student-list"; // Sửa lại tên view thành "student-list"
    }

    // ======================================================================
    // CÁC HÀM CRUD VÀ CHỨC NĂNG KHÁC (GIỮ NGUYÊN, KHÔNG THAY ĐỔI)
    // ======================================================================

    @PostMapping("/upload-excel")
    public String uploadExcelFile(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {
        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng chọn một file để tải lên.");
            return "redirect:/students/list";
        }
        try {
            Map<String, Object> result = studentService.importStudentsFromExcel(file);
            int successCount = (int) result.get("successCount");
            int failureCount = (int) result.get("failureCount");
            List<String> errors = (List<String>) result.get("errors");

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
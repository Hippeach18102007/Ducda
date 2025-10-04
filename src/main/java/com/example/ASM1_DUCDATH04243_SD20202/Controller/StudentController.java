package com.example.ASM1_DUCDATH04243_SD20202.Controller;


import com.example.ASM1_DUCDATH04243_SD20202.Model.StudentDetailDTO;
import com.example.ASM1_DUCDATH04243_SD20202.Model.StudentManager;
import com.example.ASM1_DUCDATH04243_SD20202.Respository.LopHocRepository;
import com.example.ASM1_DUCDATH04243_SD20202.Service.ExcelExportService;
import com.example.ASM1_DUCDATH04243_SD20202.Service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;
import java.util.Map; // Cần thiết cho Map<String, Object>

@Controller
@RequestMapping("/students")
public class StudentController {

    @Autowired
    private StudentService studentService;

    @Autowired
    private LopHocRepository lopHocRepository;

    @Autowired
    private ExcelExportService excelExportService; // Đã sắp xếp lại vị trí

    /** Hiển thị danh sách sinh viên */


    /** Hiển thị form thêm sinh viên */
    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("student", new StudentManager());
        model.addAttribute("classes", lopHocRepository.findAll());
        return "student-add";
    }
    @GetMapping("/")
    public String redirectToStudentList() {
        return "redirect:/students/list";
    }
    

    @GetMapping("/delete/{id}")
    public String deleteStudent(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        studentService.deleteStudent(id);
        redirectAttributes.addFlashAttribute("message", "Đã xóa sinh viên ID " + id + " thành công.");
        return "redirect:/students/list";
    }

    /** Xử lý Submit Form Thêm Sinh viên */
    @PostMapping("/add")
    public String addStudent(@ModelAttribute StudentManager student, RedirectAttributes redirectAttributes) {
        try {
            studentService.saveNewStudent(student);
            redirectAttributes.addFlashAttribute("message", "Thêm sinh viên thành công!");
        } catch (IllegalStateException e) {
            // Bắt lỗi khi lớp đã đầy
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            redirectAttributes.addFlashAttribute("student", student);
            return "redirect:/students/add";
        }
        return "redirect:/students/list";
    }

    /** Hiển thị form sửa thông tin sinh viên */
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

    /** Xử lý Submit Form Sửa Sinh viên */
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

    /** * Hiển thị thông tin chi tiết của MỘT sinh viên.
     * ĐÃ CẬP NHẬT: Thêm logic Dashboard học lực.
     */
    @GetMapping("/detail/{id}")
    public String showStudentDetail(@PathVariable Integer id, Model model) {
        StudentDetailDTO studentDetail = studentService.getStudentDetailsById(id);
        if (studentDetail == null) {
            return "redirect:/students/list"; // Hoặc trang 404
        }

        // 1. Tính toán Dashboard học lực
        Map<String, Object> academicSummary = studentService.calculateAcademicSummary(studentDetail);

        // 2. Thêm kết quả vào Model
        model.addAttribute("summary", academicSummary);
        model.addAttribute("student", studentDetail);

        return "student-detail";
    }

    @GetMapping("/export-excel")
    public ResponseEntity<ByteArrayResource> exportToExcel() throws IOException {
        byte[] excelBytes = excelExportService.exportStudentDetailsToExcel();

        String fileName = "BaoCaoSinhVien_Diem_" + System.currentTimeMillis() + ".xlsx";

        ByteArrayResource resource = new ByteArrayResource(excelBytes);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + fileName)
                .contentLength(excelBytes.length)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(resource);
    }
    @PostMapping("/assign-class/{id}")
    public String assignClassAutomatically(@PathVariable("id") Integer studentId, RedirectAttributes redirectAttributes) {
        try {
            String resultMessage = studentService.autoAssignClassForUnassignedStudent(studentId);

            // Nếu kết quả là thành công
            if (resultMessage.startsWith("Đã xếp")) {
                redirectAttributes.addFlashAttribute("message", resultMessage);
            } else {
                // Nếu kết quả là cảnh báo (chưa xếp được)
                redirectAttributes.addFlashAttribute("errorMessage", resultMessage);
            }

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi hệ thống khi xếp lớp: " + e.getMessage());
        }
        // Redirect về trang quản lý lớp để thấy sự thay đổi
        return "redirect:/classes/add";
    }
    @GetMapping("/list")
    public String listStudents(Model model) {
        List<StudentDetailDTO> studentDetails = studentService.getAllStudentDetails();

        // 1. Gọi phương thức tính toán dữ liệu biểu đồ Học lực
        Map<String, Object> academicChartData = studentService.calculateOverallAcademicDistribution();

        // 2. Gọi phương thức tính toán dữ liệu biểu đồ Chuyên ngành (MỚI)
        Map<String, Object> majorChartData = studentService.calculateMajorDistribution();

        // 3. Thêm dữ liệu vào model
        model.addAttribute("students", studentDetails);
        model.addAttribute("academicChartData", academicChartData); // Đổi tên key
        model.addAttribute("majorChartData", majorChartData);       // Dữ liệu biểu đồ mới

        return "student-list";
    }
    @PostMapping("/upload-excel")
    public String uploadExcelFile(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {
        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng chọn một file để tải lên.");
            return "redirect:/students/list";
        }

        try {
            studentService.importStudentsFromExcel(file);
            redirectAttributes.addFlashAttribute("message", "Nhập dữ liệu sinh viên từ file Excel thành công!");
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi nhập dữ liệu: " + e.getMessage());
        }

        return "redirect:/students/list";
    }

}
package com.example.ASM1_DUCDATH04243_SD20202.Service;

import com.example.ASM1_DUCDATH04243_SD20202.Model.*;
import com.example.ASM1_DUCDATH04243_SD20202.Respository.DiemRepository;
import com.example.ASM1_DUCDATH04243_SD20202.Respository.LopHocRepository;
import com.example.ASM1_DUCDATH04243_SD20202.Respository.StudentRepository;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StudentService {

    @Autowired
    private StudentRepository studentRepository;
    @Autowired
    private LopHocRepository lopHocRepository;
    @Autowired
    private DiemRepository diemRepository;
    @Autowired
    private EmailService emailService;

    // ... (Các phương thức khác của bạn giữ nguyên, tôi chỉ cập nhật lại hàm importStudentsFromExcel)

    /**
     * CẬP NHẬT: Import sinh viên từ Excel và trả về kết quả chi tiết.
     * @param file File Excel được upload.
     * @return Một Map chứa kết quả: số lượng thành công, thất bại, và danh sách lỗi.
     * @throws IOException Ném ra khi có lỗi đọc file.
     */
    public Map<String, Object> importStudentsFromExcel(MultipartFile file) throws IOException {
        Map<String, Object> result = new HashMap<>();
        List<String> errors = new ArrayList<>();
        int successCount = 0;
        int failureCount = 0;

        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rows = sheet.iterator();

            // Bỏ qua dòng header
            if (rows.hasNext()) {
                rows.next();
            }

            while (rows.hasNext()) {
                Row currentRow = rows.next();
                String studentName = getCellValueAsString(currentRow.getCell(0));
                String email = getCellValueAsString(currentRow.getCell(1));
                String sdt = getCellValueAsString(currentRow.getCell(2));
                String chuyenNganh = getCellValueAsString(currentRow.getCell(3));

                if (studentName.isEmpty() && email.isEmpty()) {
                    continue; // Bỏ qua dòng trống
                }

                // Kiểm tra dữ liệu cơ bản
                if (studentName.isEmpty() || chuyenNganh.isEmpty()) {
                    errors.add("Dòng " + (currentRow.getRowNum() + 1) + ": Tên sinh viên và Chuyên ngành không được để trống.");
                    failureCount++;
                    continue;
                }

                StudentManager newStudent = new StudentManager();
                newStudent.setStudentName(studentName);
                newStudent.setEmail(email);
                newStudent.setSdt(sdt);
                newStudent.setChuyenNganh(chuyenNganh);

                try {
                    // Tái sử dụng logic nghiệp vụ đã có để tự động xếp lớp và kiểm tra
                    this.saveNewStudent(newStudent);
                    successCount++;
                } catch (IllegalStateException e) {
                    // Bắt lỗi từ saveNewStudent (vd: lớp đầy, chuyên ngành không khớp)
                    errors.add("Sinh viên '" + newStudent.getStudentName() + "': " + e.getMessage());
                    failureCount++;
                }
            }
        }

        result.put("successCount", successCount);
        result.put("failureCount", failureCount);
        result.put("errors", errors);

        return result;
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> String.format("%.0f", cell.getNumericCellValue());
            default -> "";
        };
    }

    // ----------------------------------------------------------------------------------
    // CÁC HÀM KHÁC CỦA BẠN (GIỮ NGUYÊN)
    // Tôi copy lại các hàm khác của bạn ở đây để đảm bảo file đầy đủ
    // ----------------------------------------------------------------------------------

    private void updateClassStudentCount(Integer classId) {
        ClassManager classManager = lopHocRepository.findById(classId);
        if (classManager != null) {
            long currentCount = studentRepository.countStudentsByClassId(classId);
            classManager.setSoLuongSinhVienHienTai((int) currentCount);
            lopHocRepository.update(classManager);
        }
    }

    public List<StudentDetailDTO> getAllStudentDetails() {
        Map<Integer, ClassManager> updatedClasses = lopHocRepository.findAll().stream()
                .collect(Collectors.toMap(ClassManager::getIdClass, cls -> {
                    updateClassStudentCount(cls.getIdClass());
                    return lopHocRepository.findById(cls.getIdClass());
                }));

        return studentRepository.findAll().stream()
                .map(student -> {
                    ClassManager classInfo = updatedClasses.get(student.getIdClass());
                    List<DiemManager> scores = diemRepository.findByStudentId(student.getId());
                    return new StudentDetailDTO(student, classInfo, scores);
                }).collect(Collectors.toList());
    }

    public StudentDetailDTO getStudentDetailsById(Integer id) {
        StudentManager student = studentRepository.findById(id);
        if (student == null) return null;

        ClassManager classInfo = null;
        if (student.getIdClass() != null && !student.getIdClass().equals(0)) {
            updateClassStudentCount(student.getIdClass());
            classInfo = lopHocRepository.findById(student.getIdClass());
        }

        return new StudentDetailDTO(student, classInfo, diemRepository.findByStudentId(student.getId()));
    }

    public Map<String, Object> calculateAcademicSummary(StudentDetailDTO studentDetail) {
        Map<String, Object> summary = new HashMap<>();
        List<DiemManager> scores = studentDetail.getScores();

        if (scores == null || scores.isEmpty()) {
            summary.put("hasScores", false);
            summary.put("gpa", "0.00");
            summary.put("status", "Chưa có điểm");
            summary.put("reminder", "Sinh viên chưa có điểm học tập. Hãy thêm điểm để kích hoạt Dashboard.");
            summary.put("totalSubjects", 0);
            return summary;
        }

        summary.put("hasScores", true);
        double totalScore = scores.stream().mapToDouble(DiemManager::getDiem).sum();
        double gpa = totalScore / scores.size();
        DiemManager minScoreSubject = scores.stream().min(Comparator.comparing(DiemManager::getDiem)).orElse(null);

        String status, reminder;
        if (gpa >= 9.0) {
            status = "Xuất sắc";
            reminder = "🥇 Chúc mừng! Thành tích Xuất sắc. Hãy giữ vững phong độ đỉnh cao này!";
        } else if (gpa >= 8.0) {
            status = "Giỏi";
            reminder = "🌟 Thành tích Giỏi! Cố gắng cải thiện một chút để đạt mức Xuất sắc.";
        } else if (gpa >= 7.0) {
            status = "Khá";
            reminder = "👍 Kết quả Khá tốt. Hãy tập trung vào môn " + (minScoreSubject != null ? minScoreSubject.getMonhoc() : "") + " để cải thiện điểm TB.";
        } else if (gpa >= 5.5) {
            status = "Trung bình";
            reminder = "💡 Đã đạt yêu cầu. Cần nỗ lực hơn để không bị tụt lại.";
        } else if (gpa >= 4.0) {
            status = "Yếu";
            reminder = "⚠️ Cảnh báo: Học lực Yếu. Cần có kế hoạch học tập nghiêm túc ngay lập tức.";
        } else {
            status = "Kém";
            reminder = "🚨 Nguy hiểm! Học lực Kém. Cần sự hỗ trợ khẩn cấp để tránh bị buộc thôi học.";
        }

        summary.put("gpa", String.format("%.2f", gpa));
        summary.put("status", status);
        summary.put("reminder", reminder);
        if (minScoreSubject != null) {
            summary.put("minScore", String.format("%.2f", minScoreSubject.getDiem()));
            summary.put("subjectToImprove", minScoreSubject.getMonhoc());
        }
        summary.put("totalSubjects", scores.size());
        return summary;
    }

    public StudentManager getStudentById(Integer id) {
        return studentRepository.findById(id);
    }

    public void saveNewStudent(StudentManager student) {
        Integer idClass = student.getIdClass();
        String studentMajor = student.getChuyenNganh();

        if (idClass == null || idClass.equals(0)) {
            ClassManager classToAssign = lopHocRepository.findAllAssignableClasses().stream()
                    .filter(cls -> Objects.equals(cls.getKhoaHoc(), studentMajor))
                    .filter(cls -> studentRepository.countStudentsByClassId(cls.getIdClass()) < cls.getSoLuongSinhVien())
                    .findFirst()
                    .orElse(null);
            student.setIdClass(classToAssign != null ? classToAssign.getIdClass() : 0);
        } else {
            ClassManager chosenClass = lopHocRepository.findById(idClass);
            if (chosenClass == null) throw new IllegalStateException("Lỗi: Lớp học với ID " + idClass + " không tồn tại.");
            if (!Objects.equals(chosenClass.getKhoaHoc(), studentMajor)) throw new IllegalStateException("Lỗi: Chuyên ngành của sinh viên không khớp với khoa của lớp.");
            if (studentRepository.countStudentsByClassId(idClass) >= chosenClass.getSoLuongSinhVien()) throw new IllegalStateException("Lỗi: Lớp " + chosenClass.getClassName() + " đã đủ chỉ tiêu.");
        }

        student.setId(studentRepository.getNextId());
        studentRepository.save(student);

        // Cập nhật sĩ số và GỬI EMAIL nếu xếp lớp thành công
        if (student.getIdClass() != null && !student.getIdClass().equals(0)) {
            updateClassStudentCount(student.getIdClass());

            ClassManager assignedClass = lopHocRepository.findById(student.getIdClass());
            if (assignedClass != null) {
                emailService.sendWelcomeEmail(student, assignedClass);
            }
        }
    }

    public void updateStudent(StudentManager updatedStudent) {
        StudentManager originalStudent = studentRepository.findById(updatedStudent.getId());
        if (originalStudent == null) throw new IllegalStateException("Không tìm thấy sinh viên để cập nhật.");

        Integer oldClassId = originalStudent.getIdClass();
        Integer newClassId = updatedStudent.getIdClass();

        if (newClassId != null && !newClassId.equals(0)) {
            ClassManager newClass = lopHocRepository.findById(newClassId);
            if (newClass != null) {
                if (!Objects.equals(newClass.getKhoaHoc(), updatedStudent.getChuyenNganh())) {
                    throw new IllegalStateException("Lỗi: Chuyên ngành của sinh viên không khớp với khoa của lớp mới.");
                }
                if (!Objects.equals(oldClassId, newClassId) && studentRepository.countStudentsByClassId(newClassId) >= newClass.getSoLuongSinhVien()) {
                    throw new IllegalStateException("Lỗi: Lớp " + newClass.getClassName() + " đã đủ chỉ tiêu.");
                }
            }
        }

        studentRepository.update(updatedStudent);

        if (!Objects.equals(oldClassId, newClassId)) {
            if (oldClassId != null && !oldClassId.equals(0)) updateClassStudentCount(oldClassId);
            if (newClassId != null && !newClassId.equals(0)) updateClassStudentCount(newClassId);
        }

    }

    public void deleteStudent(Integer studentId) {
        StudentManager studentToDelete = studentRepository.findById(studentId);
        if (studentToDelete == null) return;
        Integer classId = studentToDelete.getIdClass();
        diemRepository.deleteByStudentId(studentId);
        studentRepository.deleteById(studentId);
        if (classId != null && !classId.equals(0)) {
            updateClassStudentCount(classId);
        }
    }

    public Map<String, Object> calculateOverallAcademicDistribution() {
        List<StudentDetailDTO> allStudents = getAllStudentDetails();
        Map<String, Integer> statusCounts = new HashMap<>();
        int totalStudentsWithScores = 0;

        for (StudentDetailDTO student : allStudents) {
            Map<String, Object> summary = calculateAcademicSummary(student);
            if ((boolean) summary.getOrDefault("hasScores", false)) {
                String status = (String) summary.get("status");
                statusCounts.merge(status, 1, Integer::sum);
                totalStudentsWithScores++;
            }
        }
        Map<String, Object> chartData = new HashMap<>();
        chartData.put("counts", statusCounts);
        chartData.put("totalStudentsChecked", totalStudentsWithScores);
        return chartData;
    }

    public Map<String, List<StudentManager>> getUnassignedStudentsGroupedByMajor() {
        return studentRepository.findAll().stream()
                .filter(student -> student.getIdClass() == null || student.getIdClass().equals(0))
                .collect(Collectors.groupingBy(student -> Objects.toString(student.getChuyenNganh(), "Chuyên ngành chưa rõ")));
    }

    public String autoAssignClassForUnassignedStudent(Integer studentId) {
        StudentManager student = studentRepository.findById(studentId);
        if (student == null) return "Lỗi: Không tìm thấy sinh viên.";
        if (student.getIdClass() != null && !student.getIdClass().equals(0)) return "Sinh viên đã có lớp.";

        ClassManager classToAssign = lopHocRepository.findAllAssignableClasses().stream()
                .filter(cls -> Objects.equals(cls.getKhoaHoc(), student.getChuyenNganh()))
                .filter(cls -> studentRepository.countStudentsByClassId(cls.getIdClass()) < cls.getSoLuongSinhVien())
                .findFirst()
                .orElse(null);

        if (classToAssign != null) {
            student.setIdClass(classToAssign.getIdClass());
            studentRepository.update(student);
            updateClassStudentCount(classToAssign.getIdClass());
            return "Đã xếp " + student.getStudentName() + " vào lớp " + classToAssign.getClassName() + " thành công!";
        } else {
            return "Không tìm thấy lớp còn chỉ tiêu cho chuyên ngành " + student.getChuyenNganh() + ".";
        }
    }

    public Map<String, Object> calculateMajorDistribution() {
        List<StudentManager> allStudents = studentRepository.findAll();
        Map<String, Long> majorCounts = allStudents.stream()
                .collect(Collectors.groupingBy(student -> Objects.toString(student.getChuyenNganh(), "Chuyên ngành chưa rõ"), Collectors.counting()));
        Map<String, Object> chartData = new HashMap<>();
        chartData.put("majorCounts", majorCounts);
        chartData.put("totalStudents", allStudents.size());
        return chartData;
    }
    public List<AcademicWarningDTO> getAcademicWarnings() {
        List<AcademicWarningDTO> warnings = new ArrayList<>();
        List<StudentDetailDTO> allStudents = getAllStudentDetails();

        // Đặt ngưỡng cảnh báo, ví dụ GPA < 5.5
        final double WARNING_THRESHOLD = 5.5;

        for (StudentDetailDTO student : allStudents) {
            Map<String, Object> summary = calculateAcademicSummary(student);
            boolean hasScores = (boolean) summary.getOrDefault("hasScores", false);

            if (hasScores) {
                // Parse gpa từ String về double để so sánh
                double gpa = Double.parseDouble(((String) summary.get("gpa")).replace(',', '.'));

                if (gpa < WARNING_THRESHOLD) {
                    String status = (String) summary.get("status");
                    String subjectToImprove = (String) summary.getOrDefault("subjectToImprove", "N/A");

                    AcademicWarningDTO warningDTO = new AcademicWarningDTO(student, String.format("%.2f", gpa), status, subjectToImprove);
                    warnings.add(warningDTO);
                }
            }
        }

        // Sắp xếp danh sách: sinh viên có GPA thấp nhất lên đầu
        warnings.sort(Comparator.comparing(AcademicWarningDTO::getGpa));

        return warnings;
    }
}

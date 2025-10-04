package com.example.ASM1_DUCDATH04243_SD20202.Service;

import com.example.ASM1_DUCDATH04243_SD20202.Model.ClassManager;
import com.example.ASM1_DUCDATH04243_SD20202.Model.DiemManager; // Import cần thiết cho logic tính toán
import com.example.ASM1_DUCDATH04243_SD20202.Model.StudentDetailDTO;
import com.example.ASM1_DUCDATH04243_SD20202.Model.StudentManager;
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

    /**
     * Phương thức nội bộ để cập nhật sĩ số lớp (Đã có sẵn, cần sử dụng lại).
     */
    private void updateClassStudentCount(Integer classId) {
        ClassManager classManager = lopHocRepository.findById(classId);
        if (classManager != null) {
            long currentCount = studentRepository.countStudentsByClassId(classId);
            classManager.setSoLuongSinhVienHienTai((int) currentCount);
            lopHocRepository.update(classManager);
        }
    }

    /**
     * Lấy danh sách chi tiết tất cả sinh viên (kết hợp thông tin lớp và điểm).
     * ĐÃ CẬP NHẬT: Đảm bảo sĩ số lớp luôn chính xác.
     */
    public List<StudentDetailDTO> getAllStudentDetails() {
        // Tối ưu: Lấy danh sách lớp và cập nhật sĩ số MỘT LẦN cho mỗi lớp
        Map<Integer, ClassManager> updatedClasses = lopHocRepository.findAll().stream()
                .collect(Collectors.toMap(ClassManager::getIdClass, cls -> {
                    updateClassStudentCount(cls.getIdClass()); // Cập nhật sĩ số trong DB và trong object
                    return lopHocRepository.findById(cls.getIdClass()); // Lấy lại object đã được update
                }));

        return studentRepository.findAll().stream()
                .map(student -> {
                    ClassManager classInfo = updatedClasses.get(student.getIdClass());
                    List<DiemManager> scores = diemRepository.findByStudentId(student.getId());
                    return new StudentDetailDTO(student, classInfo, scores);
                }).collect(Collectors.toList());
    }

    /**
     * Lấy chi tiết một sinh viên theo ID.
     * ĐÃ CẬP NHẬT: Đảm bảo sĩ số lớp luôn chính xác.
     */
    public StudentDetailDTO getStudentDetailsById(Integer id) {
        StudentManager student = studentRepository.findById(id);
        if (student == null) {
            return null;
        }

        ClassManager classInfo = null;
        if (student.getIdClass() != null && !student.getIdClass().equals(0)) {
            // Bước quan trọng: Cập nhật sĩ số của lớp này trước khi hiển thị
            updateClassStudentCount(student.getIdClass());
            classInfo = lopHocRepository.findById(student.getIdClass());
        }

        return new StudentDetailDTO(
                student,
                classInfo,
                diemRepository.findByStudentId(student.getId())
        );
    }

    // -------------------------------------------------------------------------------------------------------------
    // START: LOGIC CHO DASHBOARD HỌC LỰC CÁ NHÂN HÓA (ĐÃ CẬP NHẬT THANG ĐIỂM)
    // -------------------------------------------------------------------------------------------------------------

    /**
     * Tính toán Điểm Trung bình (GPA), Trạng thái, và Lời nhắc cá nhân hóa cho sinh viên.
     */
    public Map<String, Object> calculateAcademicSummary(StudentDetailDTO studentDetail) {
        Map<String, Object> summary = new HashMap<>();
        List<DiemManager> scores = studentDetail.getScores();

        if (scores == null || scores.isEmpty()) {
            // Trường hợp KHÔNG CÓ ĐIỂM
            summary.put("hasScores", false);
            summary.put("gpa", "0.00");
            summary.put("status", "Chưa có điểm");
            summary.put("reminder", "Sinh viên chưa có điểm học tập. Hãy thêm điểm để kích hoạt Dashboard.");
            summary.put("totalSubjects", 0);
            return summary;
        }

        // Trường hợp CÓ ĐIỂM
        summary.put("hasScores", true);

        double totalScore = 0;
        double minScore = Double.MAX_VALUE;
        String subjectToImprove = "các môn học"; // Tên môn có điểm thấp nhất (hoặc giá trị mặc định)

        for (DiemManager diem : scores) {
            double score = diem.getDiem();
            totalScore += score;

            if (score < minScore) {
                minScore = score;
                // Giả định DiemManager có phương thức getMonhoc() để lấy tên môn học
                subjectToImprove = diem.getMonhoc();
            }
        }

        double gpa = totalScore / scores.size();
        String status;
        String reminder;

        // CẬP NHẬT PHÂN LOẠI HỌC LỰC (Xuất sắc, Giỏi, Khá, Trung bình, Yếu, Kém)
        if (gpa >= 9.0) {
            status = "Xuất sắc";
            reminder = "🥇 Chúc mừng! Thành tích Xuất sắc. Hãy giữ vững phong độ đỉnh cao này!";
        } else if (gpa >= 8.0) {
            status = "Giỏi";
            reminder = "🌟 Thành tích Giỏi! Cố gắng cải thiện một chút để đạt mức Xuất sắc.";
        } else if (gpa >= 7.0) {
            status = "Khá";
            reminder = "👍 Kết quả Khá tốt. Hãy tập trung vào môn " + subjectToImprove + " để cải thiện điểm TB.";
        } else if (gpa >= 5.5) {
            status = "Trung bình";
            reminder = "💡 Đã đạt yêu cầu. Cần nỗ lực hơn để không bị tụt lại, đặc biệt là các môn có điểm thấp.";
        } else if (gpa >= 4.0) {
            status = "Yếu";
            reminder = "⚠️ Cảnh báo: Học lực Yếu. Cần có kế hoạch học tập nghiêm túc ngay lập tức.";
        } else {
            status = "Kém";
            reminder = "🚨 Nguy hiểm! Học lực Kém. Bạn cần sự can thiệp và hỗ trợ khẩn cấp để tránh bị buộc thôi học.";
        }

        summary.put("gpa", String.format("%.2f", gpa)); // Định dạng 2 chữ số thập phân
        summary.put("status", status);
        summary.put("reminder", reminder);
        summary.put("minScore", String.format("%.2f", minScore));
        summary.put("subjectToImprove", subjectToImprove);
        summary.put("totalSubjects", scores.size());

        return summary;
    }

    // -------------------------------------------------------------------------------------------------------------
    // END: LOGIC CHO DASHBOARD HỌC LỰC CÁ NHÂN HÓA
    // -------------------------------------------------------------------------------------------------------------

    /**
     * Lấy thông tin cơ bản của sinh viên theo ID (dùng cho form edit).
     */
    public StudentManager getStudentById(Integer id) {
        return studentRepository.findById(id);
    }

    /**
     * Lưu sinh viên mới (Có kiểm tra chuyên ngành và chỉ tiêu).
     */
    public void saveNewStudent(StudentManager student) {
        Integer idClass = student.getIdClass();
        String studentMajor = student.getChuyenNganh(); // Lấy chuyên ngành của sinh viên
        ClassManager classToAssign = null; // Khởi tạo null

        // --- 1. XỬ LÝ CHỌN LỚP (TỰ ĐỘNG HOẶC THỦ CÔNG) ---
        if (idClass == null || idClass.equals(0)) { // Phân lớp tự động

            // LỌC THEO CHUYÊN NGÀNH VÀ CHỈ TIÊU TRỐNG
            classToAssign = lopHocRepository.findAllAssignableClasses().stream()
                    // BƯỚC LỌC: Chỉ xem xét các lớp có KHÓA HỌC TRÙNG VỚI CHUYÊN NGÀNH SINH VIÊN
                    .filter(cls -> cls.getKhoaHoc() != null && cls.getKhoaHoc().equals(studentMajor))
                    .filter(cls -> {
                        long currentCount = studentRepository.countStudentsByClassId(cls.getIdClass());
                        Integer maxCapacity = cls.getSoLuongSinhVien();
                        return maxCapacity != null && currentCount < maxCapacity;
                    })
                    .findFirst()
                    // CẬP NHẬT LOGIC MỚI: Nếu không tìm thấy lớp, KHÔNG ném lỗi, mà để classToAssign là null
                    .orElse(null);

            if (classToAssign != null) {
                student.setIdClass(classToAssign.getIdClass());
            } else {
                // Nếu không tìm thấy lớp phù hợp, giữ nguyên idClass = 0 (chưa xếp lớp)
                student.setIdClass(0);
            }

        } else { // Phân lớp thủ công (Người dùng đã chọn lớp)
            classToAssign = lopHocRepository.findById(idClass);

            if (classToAssign == null) {
                throw new IllegalStateException("Lỗi: Lớp học với ID " + idClass + " không tồn tại.");
            }

            // KIỂM TRA: Đảm bảo chuyên ngành sinh viên khớp với khoa của lớp
            if (classToAssign.getKhoaHoc() == null || !classToAssign.getKhoaHoc().equals(studentMajor)) {
                throw new IllegalStateException("Lỗi: Lớp " + classToAssign.getClassName() + " thuộc Khoa " + classToAssign.getKhoaHoc() + ", không khớp với chuyên ngành " + studentMajor + " của sinh viên.");
            }

            // Kiểm tra chỉ tiêu
            long currentCount = studentRepository.countStudentsByClassId(idClass);
            Integer maxCapacity = classToAssign.getSoLuongSinhVien();
            if (maxCapacity != null && currentCount >= maxCapacity) {
                throw new IllegalStateException("Lỗi: Lớp " + classToAssign.getClassName() + " đã đủ chỉ tiêu.");
            }
        }

        // --- 2. LƯU SINH VIÊN ---
        student.setId(studentRepository.getNextId());
        studentRepository.save(student);

        // --- 3. CẬP NHẬT SĨ SỐ LỚP ---
        // Chỉ cập nhật sĩ số nếu sinh viên được xếp vào lớp (idClass != 0)
        if (student.getIdClass() != null && !student.getIdClass().equals(0)) {
            updateClassStudentCount(student.getIdClass());
        }
    }

    /**
     * Cập nhật thông tin sinh viên (Có kiểm tra chuyên ngành và chỉ tiêu khi đổi lớp).
     */
    public void updateStudent(StudentManager updatedStudent) {
        StudentManager originalStudent = studentRepository.findById(updatedStudent.getId());
        if (originalStudent == null) {
            throw new IllegalStateException("Không tìm thấy sinh viên để cập nhật.");
        }

        // Lấy chuyên ngành sinh viên để kiểm tra tính hợp lệ của lớp mới
        String studentMajor = updatedStudent.getChuyenNganh();
        Integer oldClassId = originalStudent.getIdClass();
        Integer newClassId = updatedStudent.getIdClass();

        // Kiểm tra nếu có lớp mới được chọn (newClassId != 0 và khác null)
        if (newClassId != null && !newClassId.equals(0)) {
            ClassManager newClass = lopHocRepository.findById(newClassId);
            if (newClass != null) {
                // KIỂM TRA: Đảm bảo chuyên ngành sinh viên khớp với khoa của lớp
                if (newClass.getKhoaHoc() == null || !newClass.getKhoaHoc().equals(studentMajor)) {
                    throw new IllegalStateException("Lỗi: Lớp " + newClass.getClassName() + " thuộc Khoa " + newClass.getKhoaHoc() + ", không khớp với chuyên ngành " + studentMajor + " của sinh viên.");
                }

                // Kiểm tra chỉ tiêu (Chỉ kiểm tra nếu lớp mới khác lớp cũ)
                if (!Objects.equals(oldClassId, newClassId)) {
                    long currentCount = studentRepository.countStudentsByClassId(newClassId);
                    Integer maxCapacity = newClass.getSoLuongSinhVien();
                    if (maxCapacity != null && currentCount >= maxCapacity) {
                        throw new IllegalStateException("Lỗi: Lớp " + newClass.getClassName() + " đã đủ chỉ tiêu.");
                    }
                }
            }
        }

        // Thực hiện cập nhật thông tin sinh viên
        studentRepository.update(updatedStudent);

        // Nếu sinh viên có sự thay đổi về lớp học, cập nhật sĩ số cho cả lớp cũ và lớp mới
        if (!Objects.equals(oldClassId, newClassId)) {
            // Cập nhật lớp cũ (nếu nó không phải là null/0)
            if (oldClassId != null && !oldClassId.equals(0)) {
                updateClassStudentCount(oldClassId);
            }
            // Cập nhật lớp mới (nếu nó không phải là null/0)
            if (newClassId != null && !newClassId.equals(0)) {
                updateClassStudentCount(newClassId);
            }
        }
    }

    public void deleteStudent(Integer studentId) {
        // Tìm sinh viên để biết họ thuộc lớp nào trước khi xóa
        StudentManager studentToDelete = studentRepository.findById(studentId);
        if (studentToDelete == null) {
            // Nếu không tìm thấy sinh viên thì không cần làm gì thêm
            return;
        }

        Integer classId = studentToDelete.getIdClass();

        // Thực hiện xóa điểm và thông tin sinh viên
        diemRepository.deleteByStudentId(studentId);
        studentRepository.deleteById(studentId);

        // Nếu sinh viên này đã được xếp lớp, cập nhật lại sĩ số của lớp đó
        if (classId != null && !classId.equals(0)) {
            updateClassStudentCount(classId);
        }
    }

    public Map<String, Object> calculateOverallAcademicDistribution() {
        // 1. Lấy chi tiết điểm của TẤT CẢ sinh viên
        List<StudentDetailDTO> allStudents = getAllStudentDetails();
        int totalStudentsWithScores = 0;

        // Khởi tạo Map để đếm số lượng sinh viên ở mỗi loại
        Map<String, Integer> statusCounts = new HashMap<>();
        statusCounts.put("Xuất sắc", 0);
        statusCounts.put("Giỏi", 0);
        statusCounts.put("Khá", 0);
        statusCounts.put("Trung bình", 0);
        statusCounts.put("Yếu", 0);
        statusCounts.put("Kém", 0);

        for (StudentDetailDTO student : allStudents) {
            // Sử dụng lại logic phân loại học lực để lấy trạng thái (status)
            Map<String, Object> summary = calculateAcademicSummary(student);
            boolean hasScores = (boolean) summary.getOrDefault("hasScores", false);

            if (hasScores) {
                String status = (String) summary.get("status");
                statusCounts.put(status, statusCounts.get(status) + 1);
                totalStudentsWithScores++;
            }
        }

        Map<String, Object> chartData = new HashMap<>();
        chartData.put("totalStudentsChecked", totalStudentsWithScores);

        // 2. Chuyển đổi số lượng thành tỷ lệ phần trăm (hoặc chỉ cần số lượng)
        Map<String, Double> percentageData = new HashMap<>();

        if (totalStudentsWithScores > 0) {
            for (Map.Entry<String, Integer> entry : statusCounts.entrySet()) {
                double percentage = (entry.getValue() * 100.0) / totalStudentsWithScores;
                // Lưu dưới dạng chuỗi có 2 chữ số thập phân
                percentageData.put(entry.getKey(), Double.parseDouble(String.format("%.2f", percentage)));
            }
        }

        chartData.put("counts", statusCounts); // Số lượng (tốt hơn cho Chart.js)
        chartData.put("percentages", percentageData); // Tỷ lệ (nếu cần)

        return chartData;
    }
    public Map<String, List<StudentManager>> getUnassignedStudentsGroupedByMajor() {
        return studentRepository.findAll().stream()
                .filter(student -> student.getIdClass() == null || student.getIdClass().equals(0))
                .collect(Collectors.groupingBy(
                        // Nhóm theo chuyên ngành. Sử dụng "Chưa rõ" nếu chuyenNganh là null/empty
                        student -> student.getChuyenNganh() != null && !student.getChuyenNganh().isEmpty()
                                ? student.getChuyenNganh()
                                : "Chuyên ngành chưa rõ"
                ));
    }
    public String autoAssignClassForUnassignedStudent(Integer studentId) {
        StudentManager student = studentRepository.findById(studentId);

        if (student == null) {
            return "Lỗi: Không tìm thấy sinh viên có ID " + studentId + ".";
        }

        // Kiểm tra sinh viên đã có lớp chưa (chỉ xử lý khi idClass là null/0)
        if (student.getIdClass() != null && !student.getIdClass().equals(0)) {
            return "Sinh viên " + student.getStudentName() + " đã có lớp.";
        }

        String studentMajor = student.getChuyenNganh();

        // 1. Tìm lớp trống phù hợp
        ClassManager classToAssign = lopHocRepository.findAllAssignableClasses().stream()
                .filter(cls -> cls.getKhoaHoc() != null && cls.getKhoaHoc().equals(studentMajor))
                .filter(cls -> {
                    long currentCount = studentRepository.countStudentsByClassId(cls.getIdClass());
                    Integer maxCapacity = cls.getSoLuongSinhVien();
                    return maxCapacity != null && currentCount < maxCapacity;
                })
                .findFirst()
                .orElse(null);

        // 2. Thực hiện xếp lớp hoặc báo cáo
        if (classToAssign != null) {
            Integer oldClassId = student.getIdClass();
            student.setIdClass(classToAssign.getIdClass());
            studentRepository.update(student); // Dùng update để lưu thay đổi

            // Cập nhật sĩ số lớp mới
            updateClassStudentCount(classToAssign.getIdClass());

            // Cập nhật sĩ số lớp cũ (nếu idClass trước đó không phải 0/null)
            if (oldClassId != null && !oldClassId.equals(0)) {
                updateClassStudentCount(oldClassId);
            }

            return "Đã xếp " + student.getStudentName() + " vào lớp " + classToAssign.getClassName() + " thành công!";
        } else {
            return "Không tìm thấy lớp còn chỉ tiêu cho chuyên ngành " + studentMajor + ".";
        }
    }
    // Thêm phương thức này vào StudentService.java
    /**
     * Tính toán phân phối số lượng sinh viên theo chuyên ngành.
     */
    public Map<String, Object> calculateMajorDistribution() {
        List<StudentManager> allStudents = studentRepository.findAll();

        // Nhóm và đếm số lượng sinh viên theo chuyên ngành
        Map<String, Long> majorCounts = allStudents.stream()
                .collect(Collectors.groupingBy(
                        // Sử dụng tên chuyên ngành hoặc "Chưa rõ" nếu null
                        student -> student.getChuyenNganh() != null && !student.getChuyenNganh().isEmpty()
                                ? student.getChuyenNganh()
                                : "Chuyên ngành chưa rõ",
                        Collectors.counting()
                ));

        Map<String, Object> chartData = new HashMap<>();
        chartData.put("majorCounts", majorCounts);
        chartData.put("totalStudents", allStudents.size());

        return chartData;
    }
    public void importStudentsFromExcel(MultipartFile file) throws IOException {
        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rows = sheet.iterator();

            // Bỏ qua dòng header
            if (rows.hasNext()) {
                rows.next();
            }

            // Lặp qua các dòng dữ liệu
            while (rows.hasNext()) {
                Row currentRow = rows.next();

                String studentName = getCellValueAsString(currentRow.getCell(0));
                String email = getCellValueAsString(currentRow.getCell(1));
                String sdt = getCellValueAsString(currentRow.getCell(2));
                String chuyenNganh = getCellValueAsString(currentRow.getCell(3));

                // Bỏ qua các dòng trống hoàn toàn
                if (studentName.isEmpty() && email.isEmpty() && sdt.isEmpty() && chuyenNganh.isEmpty()) {
                    continue;
                }

                StudentManager newStudent = new StudentManager();
                newStudent.setStudentName(studentName);
                newStudent.setEmail(email);
                newStudent.setSdt(sdt);
                newStudent.setChuyenNganh(chuyenNganh);

                // Lưu sinh viên, ID sẽ được repository tự động gán
                studentRepository.save(newStudent);
            }
        }
    }

    /**
     * Hàm phụ trợ để đọc giá trị của ô một cách an toàn.
     */
    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                return String.format("%.0f", cell.getNumericCellValue());
            default:
                return "";
        }
    }
}
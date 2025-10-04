package com.example.ASM1_DUCDATH04243_SD20202.Service;

import com.example.ASM1_DUCDATH04243_SD20202.Model.DiemManager;
import com.example.ASM1_DUCDATH04243_SD20202.Model.ClassManager; // Cần thiết cho phương thức mới
import com.example.ASM1_DUCDATH04243_SD20202.Respository.DiemRepository;
import com.example.ASM1_DUCDATH04243_SD20202.Respository.LopHocRepository; // Cần thiết
import com.example.ASM1_DUCDATH04243_SD20202.Respository.StudentRepository; // Cần thiết
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class DiemService {

    @Autowired
    private DiemRepository diemRepository;

    // Thêm dependencies cần thiết cho việc lọc
    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private LopHocRepository lopHocRepository; // Giả sử LopHocRepository đã được tạo

// -------------------------------------------------------------
// PHƯƠNG THỨC HỖ TRỢ LỌC TRONG FORM THÊM ĐIỂM
// -------------------------------------------------------------

    /** Lấy danh sách tất cả các chuyên ngành duy nhất từ sinh viên. */
    public Set<String> getAllUniqueMajors() {
        return studentRepository.findAll().stream()
                .map(student -> student.getChuyenNganh())
                .filter(major -> major != null && !major.isEmpty())
                .collect(Collectors.toSet());
    }

    /** Lấy danh sách tất cả các lớp học. */
    public List<ClassManager> getAllClasses() {
        // Giả sử LopHocRepository.findAll() trả về List<ClassManager>
        return lopHocRepository.findAll();
    }

// -------------------------------------------------------------
// PHƯƠNG THỨC QUẢN LÝ ĐIỂM CƠ BẢN (CRUD)
// -------------------------------------------------------------

    public void saveNewDiem(DiemManager diem) {
        // ... (Logic tạo ID và lưu)
        diem.setDiemid(diemRepository.getNextId());
        diem.setNgayCapNhat(new Date()); // Tự động gán ngày khi tạo mới
        diemRepository.save(diem);
    }

    /**
     * Lấy đối tượng DiemManager theo ID điểm (diemid) cho trang Edit.
     */
    public DiemManager getDiemById(Integer diemId) {
        return diemRepository.findByDiemId(diemId);
    }

    /**
     * CẬP NHẬT ĐIỂM: Gán lại ngày cập nhật trước khi lưu để tránh lỗi Bad Request 400.
     */
    public void updateDiem(DiemManager diem) {
        // Gán lại ngày hiện tại để cập nhật
        diem.setNgayCapNhat(new Date());

        diemRepository.update(diem);
    }

    public Integer deleteDiem(Integer diemId) {
        // 1. Tìm bản ghi điểm gốc để lấy Student ID
        DiemManager diem = diemRepository.findByDiemId(diemId);

        if (diem == null) {
            throw new IllegalArgumentException("Không tìm thấy điểm có ID: " + diemId);
        }

        Integer studentId = diem.getId(); // Giả định DiemManager.getId() là student ID (Khóa ngoại)

        // 2. Thực hiện xóa điểm
        diemRepository.deleteById(diemId);

        // 3. Trả về Student ID để Controller redirect
        return studentId;
    }
}
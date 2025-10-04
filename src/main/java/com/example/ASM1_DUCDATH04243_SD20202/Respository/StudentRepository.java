package com.example.ASM1_DUCDATH04243_SD20202.Respository;

import com.example.ASM1_DUCDATH04243_SD20202.Model.StudentManager;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Repository
public class StudentRepository {
    private static final List<StudentManager> students = new ArrayList<>();

    static {
        // Cập nhật Constructor: (id, studentName, email, idClass, sdt, chuyenNganh)
        // Gán giá trị mặc định cho thuộc tính chuyenNganh mới
        students.add(new StudentManager(1, "Nguyen Van A", "a@gmail.com", 1, "0912345678", "Công nghệ thông tin"));
        students.add(new StudentManager(2, "Tran Van B", "b@gmail.com", 1, "0987654321", "Công nghệ thông tin"));
        students.add(new StudentManager(3, "Le Thi C", "c@gmail.com", 2, "0900001111", "Công nghệ thông tin"));
    }

    public List<StudentManager> findAll() {
        return students;
    }

    public StudentManager findById(Integer id) {
        return students.stream().filter(s -> s.getId().equals(id)).findFirst().orElse(null);
    }

    public void deleteById(Integer id) {
        students.removeIf(s -> s.getId().equals(id));
    }

    public void save(StudentManager student) {
        students.add(student);
    }

    public Integer getNextId() {
        return students.stream()
                .map(StudentManager::getId)
                .max(Comparator.naturalOrder())
                .orElse(0) + 1;
    }
    public long countStudentsByClassId(Integer classId) {
        if (classId == null) {
            return 0;
        }
        return students.stream()
                .filter(sv -> classId.equals(sv.getIdClass()))
                .count();
    }
    public void update(StudentManager student) {
        // 1. Tìm chỉ mục của sinh viên cũ
        int index = -1;
        for (int i = 0; i < students.size(); i++) {
            if (students.get(i).getId().equals(student.getId())) {
                index = i;
                break;
            }
        }

        // 2. Nếu tìm thấy, thay thế
        if (index != -1) {
            // LƯU Ý: Phương thức update này sẽ tự động ghi đè sinh viên cũ
            // với sinh viên mới (đã bao gồm chuyenNganh)
            students.set(index, student);
        }
    }
    public List<StudentManager> findByClassId(Integer classId) {
        // Giả định List students là danh sách static của bạn
        return students.stream()
                .filter(s -> s.getIdClass() != null && s.getIdClass().equals(classId))
                .toList();
    }
}
package com.example.ASM1_DUCDATH04243_SD20202.Respository;

import com.example.ASM1_DUCDATH04243_SD20202.Model.ClassManager;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class LopHocRepository {
    private static final List<ClassManager> lopHocs = new ArrayList<>();

    static {
        // Cập nhật để có đủ 5 thuộc tính (idClass, className, khoaHoc, trangThai, soLuongSinhVien)
        lopHocs.add(new ClassManager(1, "CNTT1", "Công nghệ thông tin", "Đang hoạt động", 20));
        lopHocs.add(new ClassManager(2, "CNTT2", "Công nghệ thông tin", "Đang hoạt động", 15));
    }

    public List<ClassManager> findAll() {
        return lopHocs;
    }

    public ClassManager findById(Integer id) {
        return lopHocs.stream().filter(l -> l.getIdClass().equals(id)).findFirst().orElse(null);
    }

    public void save(ClassManager classManager) {
        lopHocs.add(classManager);
    }

    public Integer getNextId() {
        return lopHocs.stream()
                .map(ClassManager::getIdClass)
                .max(Comparator.naturalOrder())
                .orElse(0) + 1;
    }
    // Trong com.example.ASM1_DUCDATH04243_SD20202.Respository.LopHocRepository.java

// ... (các hàm hiện có)

    /**
     * Cập nhật thông tin lớp học bằng cách thay thế đối tượng có cùng ID.
     */
    public void update(ClassManager classManager) {
        int index = -1;
        for (int i = 0; i < lopHocs.size(); i++) {
            if (lopHocs.get(i).getIdClass().equals(classManager.getIdClass())) {
                index = i;
                break;
            }
        }

        if (index != -1) {
            lopHocs.set(index, classManager);
        }
    }
    public ClassManager findAvailableClass() {
        return lopHocs.stream()
                .filter(l -> "Đang hoạt động".equals(l.getTrangThai()))
                .findFirst() // Lấy lớp đầu tiên tìm thấy
                .orElse(null);
    }
    public List<ClassManager> findAllActiveClasses() {
        // Đảm bảo bạn đã import java.util.stream.Collectors
        return lopHocs.stream()
                .filter(l -> "Đang hoạt động".equals(l.getTrangThai()))
                .collect(Collectors.toList());
    }
    public List<ClassManager> findAllAssignableClasses() {
        return lopHocs.stream()
                .filter(l -> "Đang hoạt động".equals(l.getTrangThai()))
                .collect(Collectors.toList());
    }
    public void deleteById(Integer idClass) {
        lopHocs.removeIf(c -> c.getIdClass().equals(idClass));
    }
}
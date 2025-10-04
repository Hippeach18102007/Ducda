package com.example.ASM1_DUCDATH04243_SD20202.Service;


import com.example.ASM1_DUCDATH04243_SD20202.Model.ClassManager;
import com.example.ASM1_DUCDATH04243_SD20202.Respository.LopHocRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ClassService {

    @Autowired
    private LopHocRepository lopHocRepository;



    public void saveNewClass(ClassManager classManager) {
        // Get the next available ID from the repository
        Integer nextId = lopHocRepository.getNextId();
        classManager.setIdClass(nextId);

        // Save the new class
        lopHocRepository.save(classManager);
    }

    public ClassManager getClassById(Integer id) {
        return lopHocRepository.findById(id);
    }

    // Hàm cần thiết để Controller cập nhật lớp học
    public void updateClass(ClassManager classManager) {
        lopHocRepository.update(classManager); // Giả định Repository có hàm update
    }
// Trong com.example.ASM1_DUCDATH04243_SD20202.Respository.LopHocRepository.java

// ... (các hàm hiện có)

    /**
     * Tìm ID lớp học đầu tiên có trạng thái "Đang hoạt động".



    /**
     * Tìm ID lớp học đầu tiên có trạng thái "Đang hoạt động".
     */
    public ClassManager findAvailableClass() {
        // Lỗi: Cannot resolve method 'stream' in 'LopHocRepository'
        // KHẮC PHỤC: Gọi lopHocRepository.findAll() để lấy List trước khi gọi stream()
        return lopHocRepository.findAll().stream()
                .filter(l -> "Đang hoạt động".equals(l.getTrangThai()))
                .findFirst()
                .orElse(null);
    }

}
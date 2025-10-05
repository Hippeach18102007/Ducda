package com.example.ASM1_DUCDATH04243_SD20202.Service;

import com.example.ASM1_DUCDATH04243_SD20202.Model.ClassDetailDTO;
import com.example.ASM1_DUCDATH04243_SD20202.Model.ClassManager;
import com.example.ASM1_DUCDATH04243_SD20202.Respository.LopHocRepository;
import com.example.ASM1_DUCDATH04243_SD20202.Respository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ClassService {

    @Autowired
    private LopHocRepository lopHocRepository;

    @Autowired
    private StudentRepository studentRepository;

    // ==================================================================
    // PHƯƠNG THỨC CHO DASHBOARD VÀ HIỂN THỊ DANH SÁCH LỚP
    // ==================================================================

    public List<ClassDetailDTO> getAllClassDetails() {
        return lopHocRepository.findAll().stream()
                .map(classManager -> {
                    long currentStudents = studentRepository.countStudentsByClassId(classManager.getIdClass());
                    return new ClassDetailDTO(classManager, currentStudents);
                })
                .collect(Collectors.toList());
    }

    public Map<String, Long> getDashboardStats() {
        Map<String, Long> stats = new HashMap<>();
        long totalClasses = lopHocRepository.findAll().size();
        long totalStudents = studentRepository.findAll().size();
        long unassignedStudents = studentRepository.countUnassignedStudents();

        stats.put("totalClasses", totalClasses);
        stats.put("totalStudents", totalStudents);
        stats.put("assignedStudents", totalStudents - unassignedStudents);
        stats.put("unassignedStudents", unassignedStudents);

        return stats;
    }

    // ==================================================================
    // PHƯƠNG THỨC CRUD CHO LỚP HỌC (THÊM/SỬA/TÌM)
    // ==================================================================

    public void saveNewClass(ClassManager classManager) {
        Integer nextId = lopHocRepository.getNextId();
        classManager.setIdClass(nextId);
        lopHocRepository.save(classManager);
    }

    public ClassManager getClassById(Integer id) {
        return lopHocRepository.findById(id);
    }

    public void updateClass(ClassManager classManager) {
        lopHocRepository.update(classManager);
    }

    /**
     * Tìm ID lớp học đầu tiên có trạng thái "Đang hoạt động".
     */
    public ClassManager findAvailableClass() {
        return lopHocRepository.findAll().stream()
                .filter(l -> "Đang hoạt động".equals(l.getTrangThai()))
                .findFirst()
                .orElse(null);
    }
}


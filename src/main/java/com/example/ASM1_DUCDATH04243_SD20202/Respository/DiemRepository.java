package com.example.ASM1_DUCDATH04243_SD20202.Respository;

import com.example.ASM1_DUCDATH04243_SD20202.Model.DiemManager;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date; // Cần import Date
import java.util.List;

@Repository
public class DiemRepository {
    private static final List<DiemManager> diems = new ArrayList<>();

    static {
        // Cập nhật để có đủ 5 thuộc tính (diemid, diem, monhoc, id, ngayCapNhat)
        diems.add(new DiemManager(1, 10.0, "CNTT", 2, new Date())); // SV ID 2
        diems.add(new DiemManager(4, 9.6, "TKĐH", 1, new Date())); // SV ID 1
        diems.add(new DiemManager(2, 5.5, "TKĐH", 3, new Date())); // SV ID 3
    }

    public List<DiemManager> findAll() {
        return diems;
    }

    public List<DiemManager> findByStudentId(Integer studentId) {
        return diems.stream().filter(d -> d.getId().equals(studentId)).toList();
    }

    public void deleteByStudentId(Integer studentId) {
        diems.removeIf(d -> d.getId().equals(studentId));
    }

    public Integer getNextId() {
        return diems.stream()
                .map(DiemManager::getDiemid)
                .max(Comparator.naturalOrder())
                .orElse(0) + 1;
    }

    public void save(DiemManager diem) {
        diems.add(diem);
    }
    public void update(DiemManager diem) {
        int index = -1;
        for (int i = 0; i < diems.size(); i++) {
            if (diems.get(i).getDiemid().equals(diem.getDiemid())) {
                index = i;
                break;
            }
        }

        if (index != -1) {
            diems.set(index, diem);
        }
    }
    public DiemManager findByDiemId(Integer diemId) {
        return diems.stream()
                .filter(d -> d.getDiemid().equals(diemId))
                .findFirst()
                .orElse(null);
    }
    public void deleteDiemByStudentId(Integer studentId) {
        // Sử dụng removeIf để xóa các phần tử thỏa mãn điều kiện
        diems.removeIf(d -> d.getStudentId().equals(studentId));
        // Ghi chú: Nếu bạn đang sử dụng JPA/Hibernate, hàm này sẽ là @Transactional
    }
    public void deleteById(Integer diemId) {
        diems.removeIf(d -> d.getDiemid().equals(diemId));
    }

}
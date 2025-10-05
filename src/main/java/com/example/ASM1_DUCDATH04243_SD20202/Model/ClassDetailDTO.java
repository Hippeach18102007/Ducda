package com.example.ASM1_DUCDATH04243_SD20202.Model;

public class ClassDetailDTO {
    private final ClassManager classInfo;
    private final long currentStudents;

    public ClassDetailDTO(ClassManager classInfo, long currentStudents) {
        this.classInfo = classInfo;
        this.currentStudents = currentStudents;
    }

    // Các getter tiện ích cho Thymeleaf
    public Integer getIdClass() {
        return classInfo.getIdClass();
    }

    public String getClassName() {
        return classInfo.getClassName();
    }

    public String getKhoaHoc() {
        return classInfo.getKhoaHoc();
    }

    public Integer getMaxCapacity() {
        return classInfo.getSoLuongSinhVien();
    }

    public long getCurrentStudents() {
        return currentStudents;
    }

    public int getFillPercentage() {
        if (getMaxCapacity() == null || getMaxCapacity() == 0) {
            return 0;
        }
        return (int) ((currentStudents * 100.0) / getMaxCapacity());
    }
}

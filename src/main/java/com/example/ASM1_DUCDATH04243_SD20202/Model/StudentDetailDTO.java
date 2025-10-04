package com.example.ASM1_DUCDATH04243_SD20202.Model;


import com.example.ASM1_DUCDATH04243_SD20202.Model.ClassManager;
import com.example.ASM1_DUCDATH04243_SD20202.Model.DiemManager;
import com.example.ASM1_DUCDATH04243_SD20202.Model.StudentManager;
import java.util.List;

public class StudentDetailDTO {
    private Integer studentId;
    private String studentName;
    private String email;
    private String sdt;
    private String chuyenNganh; // <<<< THUỘC TÍNH MỚI: CHUYÊN NGÀNH
    private Integer idClass;
    private ClassManager classInfo;
    private List<DiemManager> scores;

    // Constructor để ánh xạ từ các Model
    public StudentDetailDTO(StudentManager student, ClassManager classInfo, List<DiemManager> scores) {
        this.studentId = student.getId();
        this.studentName = student.getStudentName();
        this.email = student.getEmail();
        this.sdt = student.getSdt();
        this.chuyenNganh = student.getChuyenNganh(); // <<<< ÁNH XẠ GIÁ TRỊ TỪ STUDENT MANAGER
        this.idClass = student.getIdClass();
        this.classInfo = classInfo;
        this.scores = scores;
    }


    // Constructor mặc định
    public StudentDetailDTO() {
    }

    // --- Getters và Setters ---
    public Integer getStudentId() {
        return studentId;
    }

    public void setStudentId(Integer studentId) {
        this.studentId = studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSdt() {
        return sdt;
    }

    public void setSdt(String sdt) {
        this.sdt = sdt;
    }

    // GETTER VÀ SETTER MỚI CHO CHUYÊN NGÀNH
    public String getChuyenNganh() {
        return chuyenNganh;
    }

    public void setChuyenNganh(String chuyenNganh) {
        this.chuyenNganh = chuyenNganh;
    }

    public Integer getIdClass() {
        return idClass;
    }

    public void setIdClass(Integer idClass) {
        this.idClass = idClass;
    }

    public ClassManager getClassInfo() {
        return classInfo;
    }

    public void setClassInfo(ClassManager classInfo) {
        this.classInfo = classInfo;
    }

    public List<DiemManager> getScores() {
        return scores;
    }

    public void setScores(List<DiemManager> scores) {
        this.scores = scores;
    }
}
package com.example.ASM1_DUCDATH04243_SD20202.Model;

public class StudentManager {
    private Integer id; // 1
    private String studentName; // 2
    private String email; // 3
    private Integer idClass; // 4: ID Lớp học (Khóa ngoại)
    private String sdt; // 5: Số điện thoại
    private String chuyenNganh; // 6: THUỘC TÍNH MỚI: Chuyên ngành

    // Cập nhật Constructor để bao gồm chuyenNganh (6 tham số)
    public StudentManager(Integer id, String studentName, String email, Integer idClass, String sdt, String chuyenNganh) {
        this.id = id;
        this.studentName = studentName;
        this.email = email;
        this.idClass = idClass;
        this.sdt = sdt;
        this.chuyenNganh = chuyenNganh; // Gán giá trị
    }

    // Constructor mặc định (cần cho Spring/Thymeleaf)
    public StudentManager() {
    }


    // Getters and Setters...

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public Integer getIdClass() {
        return idClass;
    }

    public void setIdClass(Integer idClass) {
        this.idClass = idClass;
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
}
package com.example.ASM1_DUCDATH04243_SD20202.Model;

public class ClassManager {

    // --- Fields ---
    private Integer idClass;
    private String className;
    private String khoaHoc;
    private String trangThai;
    private Integer soLuongSinhVien; // Chỉ tiêu tối đa
    private Integer soLuongSinhVienHienTai; // Sĩ số hiện tại

    // --- Constructors ---

    /**
     * Constructor mặc định (không tham số).
     * Bắt buộc phải có để Spring Boot khởi tạo đối tượng rỗng cho form.
     */
    public ClassManager() {
        // Khởi tạo sĩ số ban đầu là 0
        this.soLuongSinhVienHienTai = 0;
    }

    /**
     * Constructor đầy đủ tham số để khởi tạo đối tượng với dữ liệu ban đầu.
     */
    public ClassManager(Integer idClass, String className, String khoaHoc, String trangThai, Integer soLuongSinhVien) {
        this.idClass = idClass;
        this.className = className;
        this.khoaHoc = khoaHoc;
        this.trangThai = trangThai;
        this.soLuongSinhVien = soLuongSinhVien;
        this.soLuongSinhVienHienTai = 0; // Khi mới tạo lớp, sĩ số là 0
    }

    // --- Getters and Setters ---

    public Integer getIdClass() {
        return idClass;
    }

    public void setIdClass(Integer idClass) {
        this.idClass = idClass;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getKhoaHoc() {
        return khoaHoc;
    }

    public void setKhoaHoc(String khoaHoc) {
        this.khoaHoc = khoaHoc;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public Integer getSoLuongSinhVien() {
        return soLuongSinhVien;
    }

    public void setSoLuongSinhVien(Integer soLuongSinhVien) {
        this.soLuongSinhVien = soLuongSinhVien;
    }

    public Integer getSoLuongSinhVienHienTai() {
        return soLuongSinhVienHienTai;
    }

    public void setSoLuongSinhVienHienTai(Integer soLuongSinhVienHienTai) {
        this.soLuongSinhVienHienTai = soLuongSinhVienHienTai;
    }
}
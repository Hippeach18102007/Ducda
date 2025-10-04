package com.example.ASM1_DUCDATH04243_SD20202.Model;

import java.util.Date; // Cần thiết cho thuộc tính Date

public class DiemManager {
    private Integer diemid; // 1
    private Double diem; // 2
    private String monhoc; // 3
    private Integer id; // 4: ID Sinh viên (Khóa ngoại)
    private Date ngayCapNhat;
    private Integer studentId; // 5: Ngày điểm được nhập/cập nhật

    public DiemManager(Integer diemid, Double diem, String monhoc, Integer id, Date ngayCapNhat) {
        this.diemid = diemid;
        this.diem = diem;
        this.monhoc = monhoc;
        this.id = id;
        this.ngayCapNhat = ngayCapNhat;
    }

    // Lưu ý: Tôi đã bỏ thuộc tính 'heSoMonHoc' để ưu tiên 'ngayCapNhat' vì chỉ cần 5 thuộc tính.
    // Nếu bạn muốn 5 thuộc tính là: diemid, diem, monhoc, id, heSoMonHoc, hãy thông báo.

    public DiemManager() {
    }

    public Integer getStudentId() {
        return studentId;
    }

    public void setStudentId(Integer studentId) {
        this.studentId = studentId;
    }


    // Getters and Setters...
    public Integer getDiemid() {
        return diemid;
    }

    public void setDiemid(Integer diemid) {
        this.diemid = diemid;
    }

    public Double getDiem() {
        return diem;
    }

    public void setDiem(Double diem) {
        this.diem = diem;
    }

    public String getMonhoc() {
        return monhoc;
    }

    public void setMonhoc(String monhoc) {
        this.monhoc = monhoc;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Date getNgayCapNhat() {
        return ngayCapNhat;
    }

    public void setNgayCapNhat(Date ngayCapNhat) {
        this.ngayCapNhat = ngayCapNhat;
    }
}
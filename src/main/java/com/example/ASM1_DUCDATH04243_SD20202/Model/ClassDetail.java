package com.example.ASM1_DUCDATH04243_SD20202.Model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "class_detail")
public class ClassDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_class")
    private Integer idClass;

    @Column(name = "class_name", nullable = false)
    private String className;

    @Column(name = "khoa_hoc")
    private String khoaHoc;

    @Column(name = "trang_thai")
    private String trangThai;

    @Column(name = "so_luong_sinh_vien")
    private Integer soLuongSinhVien;

    // Mối quan hệ 1-Nhiều với StudentDetail
    // Một ClassDetail có nhiều StudentDetail
    // 'mappedBy' trỏ đến thuộc tính 'classDetail' trong class StudentDetail
    @OneToMany(mappedBy = "classDetail", fetch = FetchType.LAZY)
    private List<StudentDetail> students;
}
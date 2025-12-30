package com.example.ASM1_DUCDATH04243_SD20202.Model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "students")
public class StudentDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "student_name", nullable = false)
    private String studentName;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "phone_number")
    private String sdt;

    @Column(name = "major")
    private String chuyenNganh;

    // Mối quan hệ Nhiều-1 với ClassDetail
    // Nhiều StudentDetail thuộc về một ClassDetail
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id") // Đây là cột khóa ngoại trong bảng 'students'
    private ClassDetail classDetail;

    // Mối quan hệ 1-Nhiều với DiemDetail
    // Một StudentDetail có nhiều DiemDetail
    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DiemDetail> diemDetails;
}
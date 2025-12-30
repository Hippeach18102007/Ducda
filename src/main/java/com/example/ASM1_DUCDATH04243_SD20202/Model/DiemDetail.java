package com.example.ASM1_DUCDATH04243_SD20202.Model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@Entity
@Table(name = "diem_detail")
public class DiemDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "diem_id")
    private Integer diemid;

    @Column(name = "diem", nullable = false)
    private Double diem;

    @Column(name = "mon_hoc")
    private String monhoc;

    @Column(name = "ngay_cap_nhat")
    @Temporal(TemporalType.TIMESTAMP)
    private Date ngayCapNhat;

    // Mối quan hệ Nhiều-1 với StudentDetail
    // Nhiều DiemDetail thuộc về một StudentDetail
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id") // Đây là cột khóa ngoại trong bảng 'diem_detail'
    private StudentDetail student;
}
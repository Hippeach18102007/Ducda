package com.example.ASM1_DUCDATH04243_SD20202.Model;

public class AcademicWarningDTO {

    private final Integer studentId;
    private final String studentName;
    private final String className;
    private final String gpa;
    private final String status; // "Yếu" hoặc "Kém"
    private final String subjectToImprove; // Môn có điểm thấp nhất

    public AcademicWarningDTO(StudentDetailDTO studentDetail, String gpa, String status, String subjectToImprove) {
        this.studentId = studentDetail.getStudentId();
        this.studentName = studentDetail.getStudentName();
        this.className = studentDetail.getClassInfo() != null ? studentDetail.getClassInfo().getClassName() : "Chưa xếp lớp";
        this.gpa = gpa;
        this.status = status;
        this.subjectToImprove = subjectToImprove;
    }

    // Getters
    public Integer getStudentId() {
        return studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public String getClassName() {
        return className;
    }

    public String getGpa() {
        return gpa;
    }

    public String getStatus() {
        return status;
    }

    public String getSubjectToImprove() {
        return subjectToImprove;
    }
}

package com.example.ASM1_DUCDATH04243_SD20202.Service;

import com.example.ASM1_DUCDATH04243_SD20202.Model.DiemManager;
import com.example.ASM1_DUCDATH04243_SD20202.Model.StudentDetailDTO;
import org.apache.poi.ss.usermodel.*; // BẮT BUỘC: Đã sửa lỗi Cannot resolve symbol
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
public class ExcelExportService {

    @Autowired
    private StudentService studentService;

    public byte[] exportStudentDetailsToExcel() throws IOException {
        // Lấy tất cả dữ liệu chi tiết sinh viên (SV, Lớp, Điểm)
        List<StudentDetailDTO> details = studentService.getAllStudentDetails();

        // 1. Tạo Workbook và Sheet
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Báo cáo Sinh viên");

        // 2. Định dạng Header
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        CellStyle headerCellStyle = workbook.createCellStyle();
        headerCellStyle.setFont(headerFont);

        // 3. Tạo Header Row
        String[] columns = {"ID SV", "Tên Sinh viên", "Email", "SĐT", "Lớp", "Khoa học", "ID Điểm", "Môn học", "Điểm số"}; // THÊM SĐT
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < columns.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(headerCellStyle);
        }

        // 4. Đổ dữ liệu
        int rowNum = 1;
        for (StudentDetailDTO detail : details) {
            // Sửa lỗi: Gọi getScores() thay vì getDiems()
            if (detail.getScores().isEmpty()) {
                createDetailRow(sheet, rowNum++, detail, null);
            } else {
                // Sửa lỗi: Gọi getScores() thay vì getDiems()
                for (DiemManager diem : detail.getScores()) {
                    createDetailRow(sheet, rowNum++, detail, diem);
                }
            }
        }

        // ... (Tự động điều chỉnh độ rộng cột và ghi Workbook)
        for (int i = 0; i < columns.length; i++) {
            sheet.autoSizeColumn(i);
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();
        return outputStream.toByteArray();
    }

    // Hàm phụ trợ tạo từng dòng dữ liệu
    private void createDetailRow(Sheet sheet, int rowNum, StudentDetailDTO detail, DiemManager diem) {
        Row row = sheet.createRow(rowNum);

        // Ánh xạ dữ liệu dựa trên GETTERS MỚI CỦA DTO
        // Sửa lỗi: dùng getStudentId() thay vì detail.getStudent().getId()
        row.createCell(0).setCellValue(detail.getStudentId());
        row.createCell(1).setCellValue(detail.getStudentName());
        row.createCell(2).setCellValue(detail.getEmail());
        row.createCell(3).setCellValue(detail.getSdt()); // THÊM SĐT

        // Sửa lỗi: dùng getClassInfo() thay vì getLopHoc()
        row.createCell(4).setCellValue(detail.getClassInfo() != null ? detail.getClassInfo().getClassName() : "N/A");
        row.createCell(5).setCellValue(detail.getClassInfo() != null ? detail.getClassInfo().getKhoaHoc() : "N/A");

        // Dữ liệu Điểm
        if (diem != null) {
            row.createCell(6).setCellValue(diem.getDiemid());
            row.createCell(7).setCellValue(diem.getMonhoc());
            row.createCell(8).setCellValue(diem.getDiem());
        } else {
            row.createCell(6).setCellValue("N/A");
            row.createCell(7).setCellValue("N/A");
            row.createCell(8).setCellValue("N/A");
        }
    }
}
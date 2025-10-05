package com.example.ASM1_DUCDATH04243_SD20202.Controller;

import com.example.ASM1_DUCDATH04243_SD20202.Model.AcademicWarningDTO;
import com.example.ASM1_DUCDATH04243_SD20202.Service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/warnings")
public class WarningController {

    @Autowired
    private StudentService studentService;

    @GetMapping("/list")
    public String showWarningList(Model model) {
        List<AcademicWarningDTO> warnings = studentService.getAcademicWarnings();
        long totalStudents = studentService.getAllStudentDetails().size();

        model.addAttribute("warnings", warnings);
        model.addAttribute("totalStudents", totalStudents);
        model.addAttribute("warningCount", warnings.size());

        return "warnings-list";
    }
}

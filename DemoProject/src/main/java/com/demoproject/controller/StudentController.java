package com.demoproject.controller;

import com.demoproject.entity.Student;
import com.demoproject.service.StudentService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.ArrayList;
import java.util.List;

@Controller
public class StudentController {

    private final StudentService studentService;

    // Constructor Injection - Spring tự động inject StudentService
    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping("/home")
    public String showHome(Model model) {

        return "home"; // Tên file view: home.html
    }

    @GetMapping("/form")
    public String showForm(Model model) {
        model.addAttribute("firstName", new String()); // Khởi tạo đối tượng Student
        model.addAttribute("lastName", new String());
        model.addAttribute("mark" , new String());
        return "form"; // Tên file view: form.html
    }


    @PostMapping("/form")
    public String processSubmit(@ModelAttribute("firstName") String firstName,String lastName,String mark, Model model) {
        Student student = new Student();
        student.setFirstName(firstName);
        student.setLastName(lastName);
        student.setMarks(Integer.parseInt(mark));
        studentService.saveStudent(student);

        return "form"; // Chuyển hướng đến trang result.html
    }

    @GetMapping("/list")
    public String showList(Model model) {
        List<Student> students= new ArrayList<Student>();
        students= studentService.getAllStudents();
        model.addAttribute("students", students);
        return "list";
    }

    @PostMapping("/delete")
    public String processDelete(@ModelAttribute("student") Student student) {
        studentService.deleteStudent(student.getId());
        return "redirect:/list";
    }
}

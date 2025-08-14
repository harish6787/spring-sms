package com.project.project.Controller;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.project.project.Model.Admission;
import com.project.project.Repository.Admission_repository;

import jakarta.servlet.http.HttpServletResponse;

@Controller
public class Admission_controller {

    @Autowired
    private Admission_repository adminrep;

    @GetMapping("/admission")
    public String mainfun(Model model) {
        model.addAttribute("admission", new Admission());
        return "Admission";
    }

@PostMapping("/admissiondata")
public String admissionform(@ModelAttribute Admission admission,
                             @RequestParam("image") MultipartFile image,
                             Model model) {
    try {
        if (image == null || image.isEmpty()) {
            model.addAttribute("message", "Please upload a student image.");
            return "Admission"; // Go back to the form view
        }

        admission.setStudentImage(image.getBytes());
        adminrep.save(admission);
        model.addAttribute("message", "Admission Submitted Successfully!");
    } catch (IOException e) {
        model.addAttribute("message", "Image Upload Failed!");
    }
    return "redirect:/admission";
}


    @GetMapping("/admissionForm")
    public String viewAdmissions(Model model,
                                 @RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "5") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("fullName").ascending());
        Page<Admission> admissionPage = adminrep.findAll(pageable);

        model.addAttribute("data", admissionPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", admissionPage.getTotalPages());
        return "Admission_view";
    }

    @GetMapping("/admission/image/{id}")
    public void showImage(@PathVariable Long id, HttpServletResponse response) throws IOException {
        Admission student = adminrep.findById(id).orElse(null);
        if (student != null && student.getStudentImage() != null) {
            response.setContentType("image/jpeg");
            response.getOutputStream().write(student.getStudentImage());
            response.getOutputStream().flush();
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @GetMapping("/Admission_edi")
    public String edit(@RequestParam Long pass, Model model) {
        model.addAttribute("data", adminrep.findById(pass).orElse(null));
        return "Admission_edit";
    }

    @PostMapping("/admission_update")
    public String update(@ModelAttribute Admission admission,
                         @RequestParam("image") MultipartFile image) throws IOException {
        if (!image.isEmpty()) {
            admission.setStudentImage(image.getBytes());
        } else {
            Admission existing = adminrep.findById(admission.getId()).orElse(null);
            if (existing != null) {
                admission.setStudentImage(existing.getStudentImage());
            }
        }
        adminrep.save(admission);
        return "redirect:/admissionForm";
    }

    @GetMapping("/Admission_del")
    public String delete(@RequestParam Long remove) {
        adminrep.deleteById(remove);
        return "redirect:/admissionForm";
    }


@PostMapping("/delete-multiple-admissions")
@ResponseBody
public ResponseEntity<String> deleteMultiple(@RequestBody List<Long> ids) {
    if (ids != null && !ids.isEmpty()) {
        adminrep.deleteAllById(ids);
        return ResponseEntity.ok("Deleted");
    } else {
        return ResponseEntity.badRequest().body("No IDs provided");
    }
}


@GetMapping("/admission/view/{id}")
public String viewStudentDetails(@PathVariable Long id, Model model) {
    Admission student = adminrep.findById(id).orElse(null);
    model.addAttribute("student", student);
    return "admission_one_view"; // Thymeleaf template name
}



}

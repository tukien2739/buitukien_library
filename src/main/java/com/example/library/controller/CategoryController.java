
package com.example.library.controller;

import com.example.library.entity.Category;
import com.example.library.exception.BusinessException;
import com.example.library.service.CategoryService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    // DANH SÁCH THỂ LOẠI
    @GetMapping
    public String listCategories(Model model) {
        model.addAttribute(
                "categories",
                categoryService.findAll()
        );
        return "categories/list";
    }

    // FORM THÊM
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("category", new Category());
        return "categories/form";
    }

    // FORM SỬA
    @GetMapping("/{id}/edit")
    public String showEditForm(
            @PathVariable Long id,
            Model model) {

        model.addAttribute(
                "category",
                categoryService.findById(id)
        );

        return "categories/form";
    }

    // LƯU THỂ LOẠI
    @PostMapping("/save")
    public String saveCategory(
            @Valid @ModelAttribute("category") Category category,
            BindingResult result,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            return "categories/form";
        }

        try {
            categoryService.save(category);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Lưu thể loại thành công!"
            );

            return "redirect:/categories";

        } catch (BusinessException e) {

            result.rejectValue(
                    "name",
                    "category.error",
                    e.getMessage()
            );

            return "categories/form";
        }
    }

    // XÓA THỂ LOẠI
    @PostMapping("/{id}/delete")
    public String deleteCategory(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        try {
            categoryService.deleteById(id);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Xóa thể loại thành công!"
            );

        } catch (BusinessException e) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    e.getMessage()
            );
        }

        return "redirect:/categories";
    }
}


package com.example.library.controller;

import com.example.library.entity.Book;
import com.example.library.entity.Category;
import com.example.library.exception.BusinessException;
import com.example.library.service.BookService;
import com.example.library.service.CategoryService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Set;

@Controller
@RequestMapping("/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;
    private final CategoryService categoryService;

    // DANH SÁCH SÁCH
    @GetMapping
    public String listBooks(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "id") String sortField,
            @RequestParam(defaultValue = "desc") String sortDir,
            Model model) {

        Set<String> allowedFields = Set.of(
                "id",
                "title",
                "author",
                "publishYear",
                "quantity"
        );

        if (!allowedFields.contains(sortField)) {
            sortField = "id";
        }

        sortDir = "asc".equalsIgnoreCase(sortDir)
                ? "asc" : "desc";

        page = Math.max(0, page);
        size = Math.max(1, Math.min(size, 100));

        Sort sort = "asc".equals(sortDir)
                ? Sort.by(sortField).ascending()
                : Sort.by(sortField).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Book> bookPage = bookService.searchBooks(
                keyword, categoryId, pageable
        );

        model.addAttribute("bookPage", bookPage);
        model.addAttribute(
                "categories", categoryService.findAll()
        );
        model.addAttribute("keyword", keyword);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDir);

        return "books/list";
    }

    // FORM THÊM SÁCH
    @GetMapping("/new")
    public String showCreateForm(Model model) {

        Book book = new Book();
        book.setCategory(new Category());

        model.addAttribute("book", book);
        model.addAttribute(
                "categories", categoryService.findAll()
        );

        return "books/form";
    }

    // FORM SỬA SÁCH
    @GetMapping("/{id}/edit")
    public String showEditForm(
            @PathVariable Long id,
            Model model) {

        Book book = bookService.findById(id);

        if (book.getCategory() == null) {
            book.setCategory(new Category());
        }

        model.addAttribute("book", book);
        model.addAttribute(
                "categories", categoryService.findAll()
        );

        return "books/form";
    }

    // LƯU SÁCH
    @PostMapping("/save")
    public String saveBook(
            @Valid @ModelAttribute("book") Book book,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {

        List<Category> categories = categoryService.findAll();

        Long categoryId = book.getCategory() == null
                ? null
                : book.getCategory().getId();

        Category selectedCategory = null;

        if (categoryId != null) {
            selectedCategory = categories.stream()
                    .filter(c -> categoryId.equals(c.getId()))
                    .findFirst()
                    .orElse(null);
        }

        if (selectedCategory == null &&
                !result.hasFieldErrors("category") &&
                !result.hasFieldErrors("category.id")) {

            result.rejectValue(
                    "category",
                    "category.invalid",
                    "Vui lòng chọn thể loại hợp lệ"
            );
        }

        if (result.hasErrors()) {

            if (book.getCategory() == null) {
                book.setCategory(new Category());
            }

            model.addAttribute("categories", categories);
            return "books/form";
        }

        try {
            book.setCategory(selectedCategory);
            bookService.save(book);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Lưu thông tin sách thành công!"
            );

            return "redirect:/books";

        } catch (BusinessException e) {

            result.reject("businessError", e.getMessage());
            model.addAttribute("categories", categories);

            return "books/form";
        }
    }

    // XÓA SÁCH
    @PostMapping("/{id}/delete")
    public String deleteBook(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        try {
            bookService.deleteById(id);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Xóa sách thành công!"
            );

        } catch (BusinessException e) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    e.getMessage()
            );
        }

        return "redirect:/books";
    }
}

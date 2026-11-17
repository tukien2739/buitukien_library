package com.example.library.controller;

import com.example.library.entity.Book;
import com.example.library.entity.Borrow;
import com.example.library.exception.BusinessException;
import com.example.library.service.BookService;
import com.example.library.service.BorrowService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
@RequestMapping("/borrows")
@RequiredArgsConstructor
public class BorrowController {

    private final BorrowService borrowService;
    private final BookService bookService;

    @GetMapping
    public String listActiveBorrows(Model model) {
        model.addAttribute("borrows", borrowService.findActiveBorrows());
        return "borrows/list";
    }

    @GetMapping("/new/{bookId}")
    public String showBorrowForm(@PathVariable Long bookId, Model model, RedirectAttributes redirectAttributes) {
        Book book = bookService.findById(bookId);
        if (book.getQuantity() <= 0) {
            redirectAttributes.addFlashAttribute("errorMessage", "Sách này hiện đã hết trong kho!");
            return "redirect:/books";
        }
        Borrow borrow = new Borrow();
        borrow.setBook(book);
        borrow.setDueDate(LocalDate.now().plusDays(7)); // Gợi ý hạn trả mặc định 7 ngày
        model.addAttribute("borrow", borrow);
        return "borrows/form";
    }

    @PostMapping("/save")
    public String processBorrow(@ModelAttribute("borrow") Borrow borrow,
                                BindingResult result,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        // Nạp lại thông tin sách đầy đủ để hiển thị nếu form bị lỗi
        if (borrow.getBook() != null && borrow.getBook().getId() != null) {
            borrow.setBook(bookService.findById(borrow.getBook().getId()));
        }

        borrow.setBorrowDate(java.time.LocalDate.now());

        if (result.hasErrors()) {
            return "borrows/form";
        }

        try {
            borrowService.borrowBook(borrow);
            redirectAttributes.addFlashAttribute("successMessage", "Đăng ký mượn sách thành công!");
            return "redirect:/borrows";
        } catch (BusinessException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "borrows/form";
        }
    }

    @PostMapping("/{id}/return")
    public String returnBook(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            borrowService.returnBook(id);
            redirectAttributes.addFlashAttribute("successMessage", "Trả sách thành công! Đã cập nhật lại kho.");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/borrows";
    }
}
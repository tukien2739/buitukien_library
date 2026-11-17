
package com.example.library.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.FutureOrPresent;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "borrows")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Borrow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Sách được mượn
    @NotNull(message = "Vui lòng chọn sách")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    // Tên người mượn
    @NotBlank(message = "Tên người mượn không được để trống")
    @Column(name = "borrower_name", nullable = false)
    private String borrowerName;

    // Email người mượn
    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    @Column(name = "email", nullable = false)
    private String email;

    // Ngày mượn
    @NotNull(message = "Ngày mượn không được để trống")
    @Column(name = "borrow_date", nullable = false)
    private LocalDate borrowDate;

    // Hạn trả
    @NotNull(message = "Hạn trả không được để trống")
    @FutureOrPresent(message = "Hạn trả không được ở quá khứ")
    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    // Ngày trả
    @Column(name = "return_date")
    private LocalDate returnDate;

    // Tự động gán ngày mượn
    @PrePersist
    public void prePersist() {
        if (borrowDate == null) {
            borrowDate = LocalDate.now();
        }
    }

    // Tương thích với BorrowService hiện tại
    public String getBorrowerEmail() {
        return email;
    }

    public void setBorrowerEmail(String borrowerEmail) {
        this.email = borrowerEmail;
    }

    // Kiểm tra đã trả sách
    @Transient
    public boolean isReturned() {
        return returnDate != null;
    }

    // Kiểm tra quá hạn
    @Transient
    public boolean isOverdue() {
        return returnDate == null
                && dueDate != null
                && dueDate.isBefore(LocalDate.now());
    }
}

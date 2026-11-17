package com.example.library.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Table(name = "books")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Mã sách không được để trống")
    @Column(nullable = false, unique = true)
    private String code;

    @NotBlank(message = "Tên sách không được để trống")
    @Column(nullable = false)
    private String title;

    @NotBlank(message = "Tác giả không được để trống")
    @Column(nullable = false)
    private String author;

    @NotNull(message = "Năm xuất bản không được để trống")
    @Min(value = 1000, message = "Năm xuất bản không hợp lệ")
    @Column(name = "publish_year", nullable = false)
    private Integer publishYear;

    @NotNull(message = "Số lượng không được để trống")
    @Min(value = 0, message = "Số lượng không được nhỏ hơn 0")
    @Column(nullable = false)
    private Integer quantity;

    @NotNull(message = "Vui lòng chọn thể loại")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;
}
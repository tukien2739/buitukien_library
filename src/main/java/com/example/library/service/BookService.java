package com.example.library.service;

import com.example.library.entity.Book;
import com.example.library.exception.BusinessException;
import com.example.library.exception.ResourceNotFoundException;
import com.example.library.repository.BookRepository;
import com.example.library.repository.BorrowRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    private final BorrowRepository borrowRepository;

    public Page<Book> searchBooks(
            String keyword,
            Long categoryId,
            Pageable pageable) {

        return bookRepository.searchBooks(
                keyword, categoryId, pageable
        );
    }

    public Book findById(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() ->
                    new ResourceNotFoundException(
                        "Không tìm thấy sách với ID: " + id
                    )
                );
    }

    @Transactional
    public Book save(Book book) {

        if (book == null ||
                book.getCode() == null ||
                book.getCode().isBlank()) {
            throw new BusinessException(
                "Mã sách không được để trống!"
            );
        }

        String code = book.getCode().trim();

        boolean duplicate = book.getId() == null
                ? bookRepository.existsByCodeIgnoreCase(code)
                : bookRepository.existsByCodeIgnoreCaseAndIdNot(
                    code, book.getId()
                );

        if (duplicate) {
            throw new BusinessException(
                "Mã sách '" + code + "' đã tồn tại!"
            );
        }

        book.setCode(code);
        return bookRepository.save(book);
    }

    @Transactional
    public void deleteById(Long id) {

        Book book = findById(id);

        if (borrowRepository
                .existsByBookIdAndReturnDateIsNull(id)) {
            throw new BusinessException(
                "Không thể xóa sách đang được mượn!"
            );
        }

        bookRepository.delete(book);
    }
}

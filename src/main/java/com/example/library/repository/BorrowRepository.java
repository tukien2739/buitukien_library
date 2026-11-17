
package com.example.library.repository;

import com.example.library.entity.Borrow;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BorrowRepository
        extends JpaRepository<Borrow, Long> {

    // Danh sách phiếu chưa trả, sắp xếp theo hạn trả
    List<Borrow> findByReturnDateIsNullOrderByDueDateAsc();

    // Kiểm tra email đang mượn trùng một cuốn sách
    boolean existsByEmailIgnoreCaseAndBookIdAndReturnDateIsNull(
            String email,
            Long bookId
    );

    // Kiểm tra sách có phiếu mượn chưa trả
    boolean existsByBookIdAndReturnDateIsNull(
            Long bookId
    );
}

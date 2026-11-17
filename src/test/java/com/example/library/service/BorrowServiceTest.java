package com.example.library.service;

import com.example.library.entity.Book;
import com.example.library.entity.Borrow;
import com.example.library.exception.BusinessException;
import com.example.library.repository.BookRepository;
import com.example.library.repository.BorrowRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BorrowServiceTest {

    @Mock
    private BorrowRepository borrowRepository;

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private BorrowService borrowService;

    private Book book;
    private Borrow borrow;

    @BeforeEach
    void setUp() {
        book = new Book();
        book.setId(1L);
        book.setCode("B001");
        book.setTitle("Lap trinh Java");
        book.setQuantity(5);

        borrow = new Borrow();
        borrow.setBook(book);
        borrow.setBorrowerName("Nguyen Van A");
        borrow.setBorrowerEmail("test@example.com");
        borrow.setDueDate(LocalDate.now().plusDays(7));
    }

    // TEST 1: Muon sach thanh cong
    @Test
    void borrowBook_success_decreasesQuantity() {

        when(bookRepository.findById(1L))
                .thenReturn(Optional.of(book));

        when(borrowRepository
                .existsByEmailIgnoreCaseAndBookIdAndReturnDateIsNull(
                        "test@example.com", 1L))
                .thenReturn(false);

        when(borrowRepository.save(any(Borrow.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Borrow result = borrowService.borrowBook(borrow);

        assertNotNull(result);
        assertEquals(4, book.getQuantity());
        assertEquals(LocalDate.now(), result.getBorrowDate());
        assertNull(result.getReturnDate());

        verify(bookRepository).save(book);
        verify(borrowRepository).save(borrow);
    }

    // TEST 2: Khong cho muon khi het sach
    @Test
    void borrowBook_outOfStock_throwsException() {

        book.setQuantity(0);

        when(bookRepository.findById(1L))
                .thenReturn(Optional.of(book));

        assertThrows(
                BusinessException.class,
                () -> borrowService.borrowBook(borrow)
        );

        assertEquals(0, book.getQuantity());

        verify(bookRepository, never()).save(any(Book.class));
        verify(borrowRepository, never()).save(any(Borrow.class));
    }

    // TEST 3: Khong cho muon trung email va sach
    @Test
    void borrowBook_duplicateEmail_throwsException() {

        when(bookRepository.findById(1L))
                .thenReturn(Optional.of(book));

        when(borrowRepository
                .existsByEmailIgnoreCaseAndBookIdAndReturnDateIsNull(
                        "test@example.com", 1L))
                .thenReturn(true);

        assertThrows(
                BusinessException.class,
                () -> borrowService.borrowBook(borrow)
        );

        assertEquals(5, book.getQuantity());

        verify(bookRepository, never()).save(any(Book.class));
        verify(borrowRepository, never()).save(any(Borrow.class));
    }

    // TEST 4: Tra sach thanh cong
    @Test
    void returnBook_success_increasesQuantity() {

        book.setQuantity(4);
        borrow.setId(10L);
        borrow.setReturnDate(null);

        when(borrowRepository.findById(10L))
                .thenReturn(Optional.of(borrow));

        when(borrowRepository.save(any(Borrow.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Borrow result = borrowService.returnBook(10L);

        assertNotNull(result);
        assertEquals(5, book.getQuantity());
        assertEquals(LocalDate.now(), result.getReturnDate());

        verify(bookRepository).save(book);
        verify(borrowRepository).save(borrow);
    }

    // TEST 5: Khong cho muon neu han tra trong qua khu
    @Test
    void borrowBook_pastDueDate_throwsException() {

        borrow.setDueDate(LocalDate.now().minusDays(1));

        when(bookRepository.findById(1L))
                .thenReturn(Optional.of(book));

        assertThrows(
                BusinessException.class,
                () -> borrowService.borrowBook(borrow)
        );

        assertEquals(5, book.getQuantity());

        verify(bookRepository, never()).save(any(Book.class));
        verify(borrowRepository, never()).save(any(Borrow.class));
    }
}

package com.library.system;

import com.library.system.dto.BookRequestDto;
import com.library.system.dto.BookResponseDto;
import com.library.system.model.Book;
import com.library.system.model.Borrower;
import com.library.system.model.Loan;
import com.library.system.exception.ResourceNotFoundException;
import com.library.system.repository.BookRepository;
import com.library.system.repository.LoanRepository;
import com.library.system.service.BookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private LoanRepository loanRepository;

    @InjectMocks
    private BookService bookService;

    private Book testBook;
    private Borrower testBorrower;

    @BeforeEach
    void setUp() {
        testBook = new Book(1L, "9780132350884", "Clean Code", "Robert C. Martin");
        testBorrower = new Borrower(1L, "John Doe", "john@example.com");
    }

    @Test
    @DisplayName("getById: Returns isAvailable = true when no active loan exists")
    void getById_WhenBookIsAvailable_ReturnsTrue() {
        // Given
        given(bookRepository.findById(1L)).willReturn(Optional.of(testBook));
        given(loanRepository.findActiveLoanByBookId(1L)).willReturn(Optional.empty());

        // When
        BookResponseDto response = bookService.getById(1L);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.isAvailable()).isTrue();
    }

    @Test
    @DisplayName("getById: Returns isAvailable = false when an active loan exists")
    void getById_WhenBookIsLoaned_ReturnsFalse() {
        // Given
        Loan activeLoan = new Loan(10L, testBook, testBorrower, LocalDate.now().minusDays(2).atStartOfDay(), null);
        given(bookRepository.findById(1L)).willReturn(Optional.of(testBook));
        given(loanRepository.findActiveLoanByBookId(1L)).willReturn(Optional.of(activeLoan));

        // When
        BookResponseDto response = bookService.getById(1L);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.isAvailable()).isFalse();
    }

    @Test
    @DisplayName("getById: Throws ResourceNotFoundException when book ID does not exist")
    void getById_NotFound_ThrowsException() {
        // Given
        given(bookRepository.findById(99L)).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> bookService.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Book not found with id: 99");
    }

    @Test
    @DisplayName("createBook: Saves and returns new book DTO")
    void createBook_Success() {
        // Given
        BookRequestDto request = new BookRequestDto("9780132350884", "Clean Code", "Robert C. Martin");
        given(bookRepository.save(any(Book.class))).willReturn(testBook);

        // When
        BookResponseDto response = bookService.register(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getTitle()).isEqualTo("Clean Code");
        verify(bookRepository).save(any(Book.class));
    }
}
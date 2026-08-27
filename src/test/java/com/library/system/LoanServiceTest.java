package com.library.system;

import com.library.system.dto.LoanResponseDto;
import com.library.system.model.Book;
import com.library.system.model.Borrower;
import com.library.system.model.Loan;
import com.library.system.exception.BookUnavailableException;
import com.library.system.exception.ResourceNotFoundException;
import com.library.system.repository.BookRepository;
import com.library.system.repository.BorrowerRepository;
import com.library.system.repository.LoanRepository;
import com.library.system.service.BorrowerService;
import com.library.system.service.LoanService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanServiceTest {

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private BorrowerRepository borrowerRepository;

    @InjectMocks
    private LoanService loanService;

    @Mock
    private BorrowerService borrowerService;

    private Book testBook;
    private Borrower testBorrower;

    @BeforeEach
    void setUp() {
        testBook = new Book(1L, "9780201633610", "Design Patterns", "Gang of Four");
        testBorrower = new Borrower(1L, "Jane Doe", "jane@example.com");
    }

    @Test
    @DisplayName("Borrow Book: Success scenario")
    void borrowBook_Success() {
        Long bookId = 1L;
        Long borrowerId = 1L;

        // 1. Stub book lookup (pessimistic lock)
        given(bookRepository.findByIdForUpdate(bookId)).willReturn(Optional.of(testBook));

        // 2. Stub borrower service
        given(borrowerService.getByIdOrThrow(borrowerId)).willReturn(testBorrower);

        // 3. Stub active loan check (empty means book is available)
        given(loanRepository.findActiveLoanByBookId(bookId)).willReturn(Optional.empty());

        // 4. Stub save call to return the created loan
        given(loanRepository.save(any(Loan.class))).willAnswer(invocation -> invocation.getArgument(0));

        // When
        LoanResponseDto result = loanService.borrowBook(bookId, borrowerId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getBookId()).isEqualTo(testBook.getId());
        assertThat(result.getBorrowerId()).isEqualTo(testBorrower.getId());

        verify(loanRepository, times(1)).save(any(Loan.class));
    }

    @Test
    @DisplayName("Borrow Book: Throws exception when book is already borrowed")
    void borrowBook_BookNotAvailable_ThrowsException() {
        Long bookId = 1L;
        Long borrowerId = 1L;
        Loan activeLoan = new Loan(10L, testBook, testBorrower, LocalDateTime.now().minusDays(1), null);

        // 1. Stub book lookup (pessimistic lock)
        given(bookRepository.findByIdForUpdate(bookId)).willReturn(Optional.of(testBook));

        // 2. Stub borrower service
        given(borrowerService.getByIdOrThrow(borrowerId)).willReturn(testBorrower);

        // 3. Stub active loan present (simulating that the book is already borrowed)
        given(loanRepository.findActiveLoanByBookId(bookId)).willReturn(Optional.of(activeLoan));

        // When & Then
        assertThatThrownBy(() -> loanService.borrowBook(bookId, borrowerId))
                .isInstanceOf(BookUnavailableException.class)
                .hasMessageContaining("Book with id 1 is currently borrowed");

        verify(loanRepository, never()).save(any(Loan.class));
    }

    @Test
    @DisplayName("Borrow Book: Throws exception when borrower does not exist")
    void borrowBook_BorrowerNotFound_ThrowsException() {
        Long bookId = 1L;
        Long borrowerId = 99L;

        // 1. Stub the book lookup using pessimistic locking
        given(bookRepository.findByIdForUpdate(bookId)).willReturn(Optional.of(testBook));

        // 2. Stub borrowerService to throw the exception when ID 99L is requested
        given(borrowerService.getByIdOrThrow(borrowerId))
                .willThrow(new ResourceNotFoundException("Borrower not found with id: " + borrowerId));

        // When & Then
        assertThatThrownBy(() -> loanService.borrowBook(bookId, borrowerId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Borrower not found with id: 99");

        // Verify loanRepository was never queried for active loans
        verify(loanRepository, never()).findActiveLoanByBookId(any());
    }

    @Test
    @DisplayName("returnBook: Successfully populates returnDate on active loan")
    void returnBook_Success() {
// Given
        Long bookId = 1L;
        Long borrowerId = 1L;

        Loan activeLoan = new Loan(10L, testBook, testBorrower, LocalDate.now().minusDays(5).atStartOfDay(), null);

        // 1. Stub the pessimistic lock check on bookRepository
        given(bookRepository.findByIdForUpdate(bookId)).willReturn(Optional.of(testBook));

        // 2. Stub active loan lookup by both bookId and borrowerId
        given(loanRepository.findActiveLoanByBookIdAndBorrowerId(bookId, borrowerId))
                .willReturn(Optional.of(activeLoan));

        // 3. Stub save call
        given(loanRepository.save(any(Loan.class))).willAnswer(invocation -> invocation.getArgument(0));

        // When
        LoanResponseDto returnedLoan = loanService.returnBook(bookId, borrowerId);

        // Then
        assertThat(returnedLoan.getReturnedAt()).isNotNull();
        verify(loanRepository).save(activeLoan);
    }

    @Test
    @DisplayName("returnBook: Throws BookUnavailableException when no active loan exists")
    void returnBook_NoActiveLoan_ThrowsException() {
        // Given
        Long bookId = 1L;
        Long borrowerId = 1L;

        given(bookRepository.findByIdForUpdate(bookId)).willReturn(Optional.of(testBook));
        given(loanRepository.findActiveLoanByBookIdAndBorrowerId(bookId, borrowerId))
                .willReturn(Optional.empty()); // No active loan found

        // When & Then
        assertThatThrownBy(() -> loanService.returnBook(bookId, borrowerId))
                .isInstanceOf(BookUnavailableException.class)
                .hasMessageContaining("No active loan found for book id 1 and borrower id 1");

        verify(loanRepository, never()).save(any(Loan.class));
    }
}
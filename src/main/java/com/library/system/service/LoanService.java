package com.library.system.service;

import com.library.system.dto.LoanResponseDto;
import com.library.system.exception.BookUnavailableException;
import com.library.system.exception.ResourceNotFoundException;
import com.library.system.model.Book;
import com.library.system.model.Borrower;
import com.library.system.model.Loan;
import com.library.system.repository.BookRepository;
import com.library.system.repository.BorrowerRepository;
import com.library.system.repository.LoanRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LoanService {

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private BorrowerRepository borrowerRepository;

    @Autowired
    private BorrowerService borrowerService;

    @Autowired
    private BookService bookService;

    @Transactional
    public LoanResponseDto borrowBook(Long bookId, Long borrowerId){
        Book book = bookRepository.findByIdForUpdate(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + bookId));

        Borrower borrower = borrowerService.getByIdOrThrow(borrowerId);

        loanRepository.findActiveLoanByBookId(bookId).ifPresent(l -> {
            throw new BookUnavailableException("Book with id " + bookId + " is currently borrowed");
        });

        Loan loan = Loan.builder()
                .book(book)
                .borrower(borrower)
                .borrowedAt(LocalDateTime.now())
                .build();

        return LoanResponseDto.from(loanRepository.save(loan));

    }

    @Transactional
    public LoanResponseDto returnBook(Long bookId,Long borrowerId){

        bookRepository.findByIdForUpdate(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + bookId));

        Loan loan = loanRepository.findActiveLoanByBookIdAndBorrowerId(bookId, borrowerId)
                .orElseThrow(() -> new BookUnavailableException(
                        "No active loan found for book id " + bookId + " and borrower id " + borrowerId));

        loan.setReturnedAt(LocalDateTime.now());
        return LoanResponseDto.from(loanRepository.save(loan));

    }

    public List<LoanResponseDto> findAll(){
        return loanRepository.findAll().stream()
                .map(LoanResponseDto::from).collect(Collectors.toList());
    }
}

package com.library.system.service;

import com.library.system.dto.BookRequestDto;
import com.library.system.dto.BookResponseDto;
import com.library.system.exception.IsbnConflictException;
import com.library.system.exception.ResourceNotFoundException;
import com.library.system.model.Book;
import com.library.system.model.Loan;
import com.library.system.repository.BookRepository;
import com.library.system.repository.LoanRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookService {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private LoanRepository loanRepository;


    @Transactional
    public BookResponseDto register(BookRequestDto request) {
        List<Book> existingWithIsbn = bookRepository.findByIsbn(request.getIsbn());
        for (Book existing : existingWithIsbn) {
            boolean titleMatches = existing.getTitle().equalsIgnoreCase(request.getTitle());
            boolean authorMatches = existing.getAuthor().equalsIgnoreCase(request.getAuthor());
            if (!titleMatches || !authorMatches) {
                throw new IsbnConflictException(
                        "ISBN " + request.getIsbn() + " is already registered with title '"
                                + existing.getTitle() + "' by '" + existing.getAuthor() + "'");
            }
        }

        Book book = Book.builder()
                .title(request.getTitle())
                .author(request.getAuthor())
                .isbn(request.getIsbn())
                .build();
        Book saved = bookRepository.save(book);
        return BookResponseDto.from(saved, true);
    }

    public List<BookResponseDto> listAll() {
        return bookRepository.findAll().stream()
                .map(book -> BookResponseDto.from(book, loanRepository.findActiveLoanByBookId(book.getId()).isEmpty()))
                .toList();
    }

    public Book getByIdOrThrow(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + id));
    }

    public BookResponseDto getById(Long id){
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + id));
        Loan loan = loanRepository.findActiveLoanByBookId(book.getId()).orElse(null);

        return BookResponseDto.from(book,loan==null);
    }

}

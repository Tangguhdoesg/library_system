package com.library.system.repository;

import com.library.system.model.Loan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LoanRepository extends JpaRepository<Loan,Long> {

    @Query("SELECT l FROM Loan l WHERE l.book.id = :bookId AND l.returnedAt IS NULL")
    Optional<Loan> findActiveLoanByBookId(@Param("bookId") Long bookId);

    @Query("SELECT l FROM Loan l WHERE l.book.id = :bookId AND l.borrower.id = :borrowerId AND l.returnedAt IS NULL")
    Optional<Loan> findActiveLoanByBookIdAndBorrowerId(@Param("bookId") Long bookId, @Param("borrowerId") Long borrowerId);
}

package com.library.system.dto;

import com.library.system.model.Loan;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoanResponseDto {

    private Long loanId;
    private Long bookId;
    private String bookTitle;
    private Long borrowerId;
    private String borrowerName;
    private LocalDateTime borrowedAt;
    private LocalDateTime returnedAt;

    public static LoanResponseDto from(Loan l){
        return new LoanResponseDto(
                l.getId(),
                l.getBook().getId(),
                l.getBook().getTitle(),
                l.getBorrower().getId(),
                l.getBorrower().getName(),
                l.getBorrowedAt(),
                l.getReturnedAt() != null ? l.getReturnedAt() : null
        );
    }
}

package com.library.system.dto;

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
}

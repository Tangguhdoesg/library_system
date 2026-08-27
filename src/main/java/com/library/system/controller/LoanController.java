package com.library.system.controller;

import com.library.system.dto.LoanResponseDto;
import com.library.system.service.LoanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/loan")
public class LoanController {

    @Autowired
    private LoanService loanService;

    @PostMapping("/borrow")
    public ResponseEntity<LoanResponseDto> borrow(
            @RequestParam Long bookId,
            @RequestParam Long borrowerId
    ){
        return ResponseEntity.status(HttpStatus.CREATED).body(loanService.borrowBook(bookId,borrowerId));
    }

    @PostMapping("/return")
    public ResponseEntity<LoanResponseDto> retrun(
            @RequestParam Long bookId,
            @RequestParam Long borrowerId
    ){
        return ResponseEntity.status(HttpStatus.OK).body(loanService.returnBook(bookId,borrowerId));
    }

    @GetMapping
    public ResponseEntity<List<LoanResponseDto>> findALl(){
        return ResponseEntity.status(HttpStatus.OK).body(loanService.findAll());
    }

}

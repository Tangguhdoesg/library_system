package com.library.system.controller;

import com.library.system.dto.BorrowerRequestDto;
import com.library.system.dto.BorrowerResponseDto;
import com.library.system.model.Borrower;
import com.library.system.service.BorrowerService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/borrowers")
public class BorrowerController {

    @Autowired
    private BorrowerService borrowerService;

    @PostMapping
    public ResponseEntity<BorrowerResponseDto> register(
            @Valid @RequestBody BorrowerRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(borrowerService.register(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BorrowerResponseDto> getById(
            @PathVariable Long id){
        return  ResponseEntity.status(HttpStatus.OK).body(borrowerService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<BorrowerResponseDto>> getAll(){
        return  ResponseEntity.status(HttpStatus.OK).body(borrowerService.getAll());
    }

    @DeleteMapping
    public ResponseEntity<String> deleteById(
            @PathVariable Long id){
        return  ResponseEntity.status(HttpStatus.OK).body(borrowerService.deleteBorrower(id));
    }




}

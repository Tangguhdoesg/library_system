package com.library.system.controller;

import com.library.system.dto.BookRequestDto;
import com.library.system.dto.BookResponseDto;
import com.library.system.service.BookService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/books")
public class BookController {

    @Autowired
    private BookService bookService;

    @PostMapping
    public ResponseEntity<BookResponseDto> register(@Valid @RequestBody BookRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bookService.register(request));
    }

    @GetMapping
    public ResponseEntity<List<BookResponseDto>> listAll() {
        return ResponseEntity.ok(bookService.listAll());
    }


    @GetMapping("/{id}")
    public ResponseEntity<BookResponseDto> getById(@PathVariable Long id){
        return ResponseEntity.status(HttpStatus.OK).body(bookService.getById(id));
    }
}

package com.library.system;

import com.library.system.controller.BookController;
import com.library.system.dto.BookRequestDto;
import com.library.system.dto.BookResponseDto;
import com.library.system.service.BookService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookController.class)
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private BookService bookService;

    @Test
    @DisplayName("GET /api/books - Should return list of books")
    void getAllBooks_ShouldReturnListOfBooksAndStatus200() throws Exception {
        // Given
        BookResponseDto book = BookResponseDto.builder()
                .isbn("9780132350884")
                .title("Clean Code")
                .author("Robert C. Martin")
                .id(1L)
                .build();

        given(bookService.listAll()).willReturn(List.of(book));
        // When & Then
        mockMvc.perform(get("/api/books")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("Clean Code"))
                .andExpect(jsonPath("$[0].author").value("Robert C. Martin"));
    }

    @Test
    @DisplayName("GET /api/books/{id} - Should return book by ID")
    void getById_ShouldReturnBook() throws Exception {
        // Given
        BookResponseDto responseDto = new BookResponseDto(1L, "Clean Code", "Robert C. Martin", "9780132350884", false);
        given(bookService.getById(1L)).willReturn(responseDto);

        // When & Then
        mockMvc.perform(get("/api/books/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("POST /api/books - Should create book when request is valid")
    void createBook_WhenValidInput_ShouldReturnCreatedBookAndStatus200Or201() throws Exception {
        // Given
        BookRequestDto requestDto = new BookRequestDto("Clean Code", "Robert C. Martin", "9780132350884");
        BookResponseDto responseDto = BookResponseDto.builder()
                .id(1L)
                .isbn("9780132350884")
                .title("Clean Code")
                .author("Robert C. Martin")
                .build();

        given(bookService.register(any(BookRequestDto.class))).willReturn(responseDto);

        // When & Then
        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Clean Code"));
    }

    @Test
    @DisplayName("POST /api/books - Should return 400 Bad Request on invalid input")
    void createBook_WhenInvalidInput_ShouldReturnStatusBadRequest() throws Exception {
        // Given: Missing required title field
        BookRequestDto invalidRequest = new BookRequestDto("9780132350884", "", "Robert C. Martin");

        // When & Then
        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
}
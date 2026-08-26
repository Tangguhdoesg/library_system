package com.library.system.dto;

import com.library.system.model.Book;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookResponseDto {

    private Long id;
    private String isbn;
    private String title;
    private String author;
    private boolean available;

    public static BookResponseDto from(Book b, boolean available){
        return new BookResponseDto(b.getId(), b.getIsbn(), b.getTitle(), b.getIsbn(),available);
    }

}

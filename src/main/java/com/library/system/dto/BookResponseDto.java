package com.library.system.dto;

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

}

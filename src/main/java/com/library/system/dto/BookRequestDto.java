package com.library.system.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookRequestDto {

    private String isbn;
    @NotBlank(message = "Title is required")
    private String title;
    private String author;


}

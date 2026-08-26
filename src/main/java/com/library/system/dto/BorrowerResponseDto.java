package com.library.system.dto;

import com.library.system.model.Borrower;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BorrowerResponseDto {

    private Long id;
    private String name;
    private String email;

    public static BorrowerResponseDto from(Borrower b) {
        return new BorrowerResponseDto(b.getId(), b.getName(), b.getEmail());
    }
}

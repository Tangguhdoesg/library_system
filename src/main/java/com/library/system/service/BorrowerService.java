package com.library.system.service;

import com.library.system.dto.BorrowerRequestDto;
import com.library.system.dto.BorrowerResponseDto;
import com.library.system.exception.ResourceNotFoundException;
import com.library.system.model.Borrower;
import com.library.system.repository.BorrowerRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BorrowerService {

    @Autowired
    private BorrowerRepository borrowerRepository;

    @Transactional
    public BorrowerResponseDto register(BorrowerRequestDto request) {
        borrowerRepository.findByEmail(request.getEmail()).ifPresent(b -> {
            throw new IllegalArgumentException("A borrower with this email already exists");
        });

        Borrower borrower = Borrower.builder()
                .email(request.getEmail())
                .name(request.getName())
                .build();

        Borrower saved = borrowerRepository.save(borrower);
        return BorrowerResponseDto.from(saved);
    }

    public Borrower getByIdOrThrow(Long id) {
        return borrowerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Borrower not found with id: " + id));
    }

    public BorrowerResponseDto getById(Long id){
        return BorrowerResponseDto.from(getByIdOrThrow(id));
    }

    public List<BorrowerResponseDto> getAll() {
        return borrowerRepository.findAll().stream()
                .map(BorrowerResponseDto::from)
                .collect(Collectors.toList());
    }

}

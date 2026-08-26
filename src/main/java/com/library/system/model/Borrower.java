package com.library.system.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name="borrower")
@Data
public class Borrower {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String email;
}

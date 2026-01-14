package com.deare.backend.domain.test;

import jakarta.persistence.*;
import lombok.Getter;

@Getter
@Entity
public class Test {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;
}

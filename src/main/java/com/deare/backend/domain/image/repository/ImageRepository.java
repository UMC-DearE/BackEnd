package com.deare.backend.domain.image.repository;

import com.deare.backend.domain.image.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ImageRepository extends JpaRepository<Image,Long> {

    Optional<Image> findByIdAndIsDeletedFalse(Long id);
}

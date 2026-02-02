package com.deare.backend.domain.emotion.repository;

import com.deare.backend.domain.emotion.entity.Emotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmotionRepository extends JpaRepository<Emotion,Long> {
    List<Emotion> findByNameIn(List<String> names);
}

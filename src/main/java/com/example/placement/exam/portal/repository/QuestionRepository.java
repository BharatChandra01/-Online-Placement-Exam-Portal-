package com.example.placement.exam.portal.repository;

import com.example.placement.exam.portal.model.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {
    // This makes the search much more reliable on Linux servers
    List<Question> findByCategoryIgnoreCase(String category);
}
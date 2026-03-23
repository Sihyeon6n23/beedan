package com.goodee.beedan.repository.category;

import com.goodee.beedan.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByCatNm(String catNm);
}

package com.goodee.beedan.repository.token;

import com.goodee.beedan.entity.Token;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Objects;
import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token, Long> {
    Optional<Token> findByTkVl(String tokenValue);
}

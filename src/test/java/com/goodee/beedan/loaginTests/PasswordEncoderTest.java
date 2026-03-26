package com.goodee.beedan.loaginTests;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;

class PasswordEncoderTest {

    // 테스트할 대상 (Bean으로 등록될 객체와 동일)
    PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Test
    @DisplayName("1234 비밀번호 암호화 및 일치 여부 테스트")
    void passwordMatchTest() {
        // Given: 사용자가 입력한 평문 비밀번호와 DB에 저장된 것으로 가정할 해시값
        String rawPassword = "1234";
        String dbEncodedPassword = "$2a$10$Tx1CyrrWN2qWI48xREs/a.H0N2WGc8jrLsdhBY/lWv53c0z1AFl/2";

        // When: PasswordEncoder를 통해 비교 연산 수행
        boolean isMatch = passwordEncoder.matches(rawPassword, dbEncodedPassword);

        // Then: 결과는 true여야 함
        assertThat(isMatch).isTrue();
    }

    @Test
    @DisplayName("매번 생성되는 해시값이 달라도 원문이 같으면 일치해야 한다")
    void saltTest() {
        // Given
        String rawPassword = "1234";

        // When: 같은 1234를 두 번 암호화 (Salt 때문에 결과가 다름)
        String encode1 = passwordEncoder.encode(rawPassword);
        String encode2 = passwordEncoder.encode(rawPassword);

        // Then
        System.out.println("Encode 1: " + encode1);
        System.out.println("Encode 2: " + encode2);

        // 두 해시값은 서로 다르지만
        assertThat(encode1).isNotEqualTo(encode2);

        // matches() 결과는 둘 다 true여야 함
        assertThat(passwordEncoder.matches(rawPassword, encode1)).isTrue();
        assertThat(passwordEncoder.matches(rawPassword, encode2)).isTrue();
    }
}
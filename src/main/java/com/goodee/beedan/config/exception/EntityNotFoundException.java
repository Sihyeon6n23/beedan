package com.goodee.beedan.config.exception;

public class EntityNotFoundException extends BusinessException {
        public EntityNotFoundException() {
            super("일치하는 데이터가 없습니다.");
        }

        public EntityNotFoundException(String message) {
            super(message);
        }
}

package com.goodee.beedan.config.exception;

public class MemberNotFoundException extends BusinessException {

    public MemberNotFoundException() {
        super("로그인 후 이용 가능합니다.");
    }

    public MemberNotFoundException(String message) {
        super(message);
    }
}

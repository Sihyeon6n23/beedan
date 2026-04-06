package com.goodee.beedan.service.board;

import com.goodee.beedan.common.constant.BoardType;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ViewCountService {
    private final BoardCoreService boardCoreService;
    private final String COOKIE_NAME = "alreadyViewedNotices";

    public void increaseViewCountWithCookie(Long boardId, HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        Cookie targetCookie = null;

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(COOKIE_NAME)) {
                    targetCookie = cookie;
                    break;
                }
            }
        }

        // 쿠키가 존재하고, 현재 게시글 번호를 포함하고 있다면 조회수 증가 X
        if (targetCookie != null) {
            if (targetCookie.getValue().contains("[" + boardId + "]")) {
                return;
            } else {
                // 기존 쿠키에 현재 게시글 번호 추가
                targetCookie.setValue(targetCookie.getValue() + "[" + boardId + "]");
            }
        } else {
            // 새 쿠키 생성
            targetCookie = new Cookie(COOKIE_NAME, "[" + boardId + "]");
        }

        // 실제 DB 조회수 증가
        boardCoreService.increaseViewCount(boardId, BoardType.NOTICE);

        // 쿠키 설정 (24시간 유지, 모든 경로에서 유효)
        targetCookie.setPath("/");
        targetCookie.setMaxAge(60 * 60 * 24); // 24시간
        targetCookie.setHttpOnly(true); // 자바스크립트 접근 방지 (보안)
        response.addCookie(targetCookie);
    }
}

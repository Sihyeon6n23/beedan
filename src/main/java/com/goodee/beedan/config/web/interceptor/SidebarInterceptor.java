package com.goodee.beedan.config.web.interceptor;

import com.goodee.beedan.config.web.annotation.Sidebar;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

@Component
public class SidebarInterceptor implements HandlerInterceptor {

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {
        // 1. 리다이렉트거나 모델이 없으면 무시
        if (modelAndView == null || modelAndView.getViewName().startsWith("redirect:")) {
            return;
        }

        // 2. 실행될 핸들러(메소드)가 실제 컨트롤러 메소드인지 확인
        if (handler instanceof HandlerMethod) {
            HandlerMethod method = (HandlerMethod) handler;

            // 3. @Sidebar 어노테이션이 붙어 있는지 확인
            Sidebar sidebar = method.getMethodAnnotation(Sidebar.class);

            if (sidebar != null) {
                String uri = request.getRequestURI();
                // 어노테이션에 값을 적었다면 그 값을 쓰고, 없으면 URI에서 추출
                String activeMenu = !sidebar.value().isEmpty() ? sidebar.value() : determineMenu(uri);
                modelAndView.addObject("activeMenu", activeMenu);
            }
        }
    }

    private String determineMenu(String uri) {
        if (uri.contains("/signup")) return "signup";
        if (uri.contains("/profile")) return "profile";
        return "none";
    }
}

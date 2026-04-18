package com.goodee.beedan.controller.main;

import com.goodee.beedan.dto.root.security.SecurityPolicyDto;
import com.goodee.beedan.dto.stock.StockListDto;
import com.goodee.beedan.entity.Member;
import com.goodee.beedan.service.member.MemberService;
import com.goodee.beedan.service.news.NewsService;
import com.goodee.beedan.service.root.SecurityService;
import com.goodee.beedan.service.stock.StockDisplayService;
import com.goodee.beedan.service.weather.WeatherService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@Slf4j
public class MainController {

    private final WeatherService weatherService;
    private final NewsService newsService;
    private final StockDisplayService stockDisplayService;
    private final MemberService memberService;
    private final SecurityService securityService;

    @GetMapping("/")
    public String getMain(HttpSession session, Principal principal, Model model,
                          HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1
        response.setHeader("Pragma", "no-cache"); // HTTP 1.0
        response.setDateHeader("Expires", 0); // Proxies

        // 날씨 정보 호출
        List<Map<String, Object>> weatherList = weatherService.getWeatherList();
        model.addAttribute("weatherList", weatherList);

        // 뉴스 목록 가져오기 1
        List<Map<String, String>> leftNewsList = newsService.getLeftNewsList();
        model.addAttribute("leftNewsList", leftNewsList);
        // 뉴스 목록 가져오기 2
        List<Map<String, String>> rightNewsList = newsService.getRightNewsList();
        model.addAttribute("rightNewsList", rightNewsList);

        // 전 월 인기상품 전시
        List<StockListDto> popularStockList = stockDisplayService.getPopularStocks();
        model.addAttribute("popularStockList", popularStockList);

        // 최근 등록 상품 전시
        List<StockListDto> newStockList = stockDisplayService.getNewStocks();
        model.addAttribute("newStockList", newStockList);

        // 비밀번호 만료검증 로직
        Boolean isJustLoggedIn = (Boolean) session.getAttribute("LOGIN_TRIGGER");
        if (Boolean.TRUE.equals(isJustLoggedIn)) {
            // 2. [핵심] 플래그가 있을 때만 실제 DB 조회 및 정책 계산 수행
            String username = principal.getName();
            Member member = memberService.getMemberByUsername(username);
            SecurityPolicyDto policy = securityService.getSecPolDto();
            // 서비스에 작성한 만료 체크 로직 호출
            if (memberService.isPasswordExpired(member)) {
                model.addAttribute("showPwChangeModal", true);
                model.addAttribute("DAY", policy.getPasswordExpiryDays());
            }
            session.removeAttribute("LOGIN_TRIGGER");
        }

        return "main/index";
    }
}

package com.goodee.beedan.devUtils;

import com.goodee.beedan.service.chat.ChatSchedulerService;
import com.goodee.beedan.service.root.SchedulerService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.Map;

@Controller
@RequestMapping("/dev")
@Profile("!prod")
public class DevController {

    private final Map<String, Runnable> schedulerMap;
    private final AuthenticationManager authenticationManager;
    private final SchedulerService schedulerService;

    public DevController(
            AuthenticationConfiguration authenticationConfiguration,
            ChatSchedulerService chatSchedulerService, SchedulerService schedulerService) throws Exception {
        this.authenticationManager = authenticationConfiguration.getAuthenticationManager();
        this.schedulerService = schedulerService;
        this.schedulerMap = new LinkedHashMap<>();

//        schedulerMap.put("schedulerA", schedluerA::run);

        // 채팅방 자동 종료 스케줄러
        schedulerMap.put("chatAutoClose", () -> {
            try {
                long inactiveHours = schedulerService.getSchedulerSetting().getChatAutoCloseInterval() !=
                        null
                        ? Long.parseLong(schedulerService.getSchedulerSetting().getChatAutoCloseInterval())
                        : 72L;
                chatSchedulerService.closeInactiveChatRooms(inactiveHours);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @PostMapping("/quick-login")
    @ResponseBody
    public Map<String, String> quickLogin(@RequestBody Map<String, String> body,
                                          HttpServletRequest request) {
        String username = body.get("username");

        // 기존 세션 무효화 (로그아웃)
        SecurityContextHolder.clearContext();
        HttpSession oldSession = request.getSession(false);
        if (oldSession != null) oldSession.invalidate();

        // 인증
        UsernamePasswordAuthenticationToken token =
                new UsernamePasswordAuthenticationToken(username, "1234");
        Authentication auth = authenticationManager.authenticate(token);

        // 새 SecurityContext 설정
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);

        HttpSession newSession = request.getSession(true);
        newSession.setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);

        return Map.of("status", "ok", "username", username);
    }

    @GetMapping
    public String devPage(Model model) {
        model.addAttribute("schedulerNames", schedulerMap.keySet());
        model.addAttribute("currentTime", AppDateTime.now());

        // 현재 로그인 사용자
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String loggedInUser = null;
        if (auth != null && auth.getPrincipal() instanceof UserDetails) {
            loggedInUser = ((UserDetails) auth.getPrincipal()).getUsername();
        }
        model.addAttribute("loggedInUser", loggedInUser);

        return "devPage/devPage";
    }

    @GetMapping("/time")
    @ResponseBody
    public String getTime() {
        return AppDateTime.now()
                .truncatedTo(ChronoUnit.SECONDS)
                .toString();
    }

    @PostMapping("/time")
    @ResponseBody
    public String setTime(@RequestBody Map<String, String> body) {
        LocalDateTime target = LocalDateTime.parse(body.get("datetime"));
        AppDateTime.setNow(target);
        return AppDateTime.now()
                .truncatedTo(ChronoUnit.SECONDS)
                .toString();
    }

    @DeleteMapping("/time")
    @ResponseBody
    public String resetTime() {
        AppDateTime.reset();
        return AppDateTime.now()
                .truncatedTo(ChronoUnit.SECONDS)
                .toString();
    }

    @PostMapping("/tick/{name}")
    @ResponseBody
    public String tick(@PathVariable String name) {
        Runnable job = schedulerMap.get(name);
        if (job == null) return "존재하지 않는 스케줄러: " + name;
        job.run();
        return "[" + AppDateTime.now().truncatedTo(ChronoUnit.SECONDS) + "] " + name + " 실행 완료";
    }
}

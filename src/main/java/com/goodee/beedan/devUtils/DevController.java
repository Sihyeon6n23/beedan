package com.goodee.beedan.devUtils;

import org.springframework.context.annotation.Profile;
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

    public DevController(

    ){
        this.schedulerMap = new LinkedHashMap<>();
//        schedulerMap.put("schedulerA", schedluerA::run);
    }

    @GetMapping
    public String devPage(Model model) {
        model.addAttribute("schedulerNames", schedulerMap.keySet());
        model.addAttribute("currentTime", AppDateTime.now());
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

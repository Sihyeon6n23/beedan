package com.goodee.beedan.controller.chat;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/chat")
public class AdminChatController {

    @GetMapping("/list")
    public String getChatListPage() {
        return "/admin/chat/chat-list";
    }

    @GetMapping("/detail")
    public String getChatDetailPage() {
        return "/admin/chat/chat-detail";
    }
}

package com.goodee.beedan.controller.chat;

import com.goodee.beedan.config.security.MemberUserDetails;
import com.goodee.beedan.dto.chat.AdminChatRoomDetailDto;
import com.goodee.beedan.dto.chat.AdminChatRoomListDto;
import com.goodee.beedan.dto.chat.AdminChatRoomSearchDto;
import com.goodee.beedan.service.chat.AdminChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/chat")
public class AdminChatController {
    private final AdminChatService adminChatService;

    // 관리자 채팅 목록
    @GetMapping("/list")
    public String getAdminChatList(Model model,
        @RequestParam(defaultValue = "ALL") String status,
        @RequestParam(defaultValue = "") String keyword,
        @RequestParam(defaultValue = "false") Boolean myAssignedOnly,
        @RequestParam(defaultValue = "0") int page,
        @AuthenticationPrincipal MemberUserDetails userDetails) {

        AdminChatRoomSearchDto searchDto = AdminChatRoomSearchDto.builder()
                .status(status)
                .keyword(keyword)
                .myAssignedOnly(myAssignedOnly)
                .page(page)
                .size(5)
                .build();

        Page<AdminChatRoomListDto> chatRoomPage = adminChatService
                .getAdminChatRooms(searchDto, userDetails.getMemberId());

        int pageBlockSize = 10;
        int startPage = (chatRoomPage.getNumber() / pageBlockSize) * pageBlockSize;
        int endPage = Math.min(startPage + pageBlockSize - 1, Math.max(chatRoomPage.getTotalPages() - 1, 0));

        model.addAttribute("chatRoomPage", chatRoomPage); // 페이지네이션용
        model.addAttribute("chatRooms", chatRoomPage.getContent()); // 목록용
        model.addAttribute("searchDto", searchDto); // 상태 + 담당 필터링용
        model.addAttribute("startPage", startPage); // 블럭용
        model.addAttribute("endPage", endPage); // 블럭용

        return "/admin/chat/chat-list";
    }

    // 관리자 채팅 상세 (채팅방)
    @GetMapping("/detail")
    public String getAdminChatDetail(Model model,
                                     @RequestParam("id") Long chRoId,
                                     @AuthenticationPrincipal MemberUserDetails userDetails) {
        AdminChatRoomDetailDto chatRoomDetail = adminChatService
                .getAdminChatRoomDetail(chRoId, userDetails.getMemberId());

        model.addAttribute("chatRoomDetail", chatRoomDetail);

        return "/admin/chat/chat-detail";
    }
}

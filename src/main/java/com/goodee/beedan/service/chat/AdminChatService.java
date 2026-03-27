package com.goodee.beedan.service.chat;

import com.goodee.beedan.repository.chat.ChatMessageRepository;
import com.goodee.beedan.repository.chat.ChatRoomReadStatusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminChatService {
    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomReadStatusRepository chatRoomReadStatusRepository;

}

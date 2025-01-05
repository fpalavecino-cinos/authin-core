package com.argctech.core.chat.service.impl;

import com.argctech.core.chat.entity.ChatMessage;
import com.argctech.core.chat.repository.ChatMessageRepository;
import com.argctech.core.chat.repository.ChatRoomRepository;
import com.argctech.core.chat.service.IChatMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class ChatMessageService implements IChatMessageService {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomService chatRoomService;

    @Override
    public ChatMessage save(ChatMessage chatMessage) {
        var chatId = chatRoomService.getChatRoomId(chatMessage.getSenderId(),
                        chatMessage.getRecipientId(),
                        true)
                .orElseThrow(() -> new IllegalArgumentException("Chat Room doesn't exist"));//TODO: Create a custom exception
        chatMessage.setChatId(chatId);
        chatMessageRepository.save(chatMessage);
        return chatMessage;
    }
    @Override
    public List<ChatMessage> findChatMessages(String senderId, String recipientId) {
        var chatId = chatRoomService.getChatRoomId(senderId, recipientId, false);
        return chatId.map(chatMessageRepository::findByChatId).orElse(new ArrayList<>());
    }


}

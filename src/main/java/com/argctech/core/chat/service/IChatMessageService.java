package com.argctech.core.chat.service;

import com.argctech.core.chat.entity.ChatMessage;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public interface IChatMessageService {
    public ChatMessage save(ChatMessage chatMessage);
    public List<ChatMessage> findChatMessages(String senderId, String recipientId);
}

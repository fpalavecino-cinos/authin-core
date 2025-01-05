package com.argctech.core.chat.service;


import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public interface IChatRoomService {
    public Optional<String> getChatRoomId(String senderId, String recipientId, boolean createNewRoomIfNoExist);
}

package com.argctech.core.chat.service.impl;

import com.argctech.core.chat.entity.ChatRoom;
import com.argctech.core.chat.repository.ChatRoomRepository;
import com.argctech.core.chat.service.IChatRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class ChatRoomService implements IChatRoomService {

    private final ChatRoomRepository chatRoomRepository;


    @Override
    public Optional<String> getChatRoomId(String senderId, String recipientId, boolean createNewRoomIfNoExist) {
        return chatRoomRepository.findBySenderIdAndRecipientId(senderId, recipientId)
                .map(chatRoom -> chatRoom.getChatId())
                .or(() -> {
                    if (createNewRoomIfNoExist) {
                        var chatId = createChatId(senderId, recipientId);
                        return Optional.of(chatId);
                    }
                    return Optional.empty();
                });
    }

    private String createChatId(String senderId, String recipientId) {

        var chatId = String.format(("%s_%s"), senderId, recipientId);
        ChatRoom senderRecipient = ChatRoom.builder()
                .chatId(chatId)
                .senderId(senderId)
                .recipientId(recipientId)
                .build();
        ChatRoom recipientSender = ChatRoom.builder()
                .chatId(chatId)
                .senderId(recipientId)
                .recipientId(senderId)
                .build();
        chatRoomRepository.save(senderRecipient);
        chatRoomRepository.save(recipientSender);

        return chatId;

    }
}

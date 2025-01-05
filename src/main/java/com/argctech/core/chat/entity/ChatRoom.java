package com.argctech.core.chat.entity;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;


@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Builder
public class ChatRoom {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    private String chatId;
    private String senderId;
    private String recipientId;

}

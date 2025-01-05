package com.argctech.core.chat.entity;


import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Setter
@Getter
public class ChatNotification {

    private String id;
    private String senderId;
    private String recipientId;
    private String content;


}

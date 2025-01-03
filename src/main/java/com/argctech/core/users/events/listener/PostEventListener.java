//package com.argctech.core.users.events.listener;
//
//import com.argctech.core.users.events.PostCreateEvent;
//import com.argctech.core.users.service.IUserService;
//import com.argctech.core.users.utils.exceptions.UserNotFoundException;
//import com.argctech.core.utils.JsonParser;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.kafka.annotation.KafkaListener;
//import org.springframework.stereotype.Component;
//
//@Component
//@Slf4j
//@RequiredArgsConstructor
//public class PostEventListener {
//
//    private final IUserService userService;
//
//    @KafkaListener(topics = "posts-create-topic")
//    public void onPostCreateEvent(String message) throws UserNotFoundException {
//        PostCreateEvent event = JsonParser.toJson(message, PostCreateEvent.class);
//        log.info("Post event received: {}", event.toString());
//    }
//
//}

package com.eatall.websocket.service;

import com.eatall.websocket.ChatMessageRepository;
import com.eatall.websocket.document.ChatMessage;
import com.eatall.websocket.dto.ChatMessageDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Transactional
@Service
@RequiredArgsConstructor
public class ChatService {
    private final ChatMessageRepository chatMessageRepository;

    public ChatMessage saveMessage(ChatMessageDTO messageDto) {
        String chatRoomId = generateChatRoomId(messageDto.getSenderId(), messageDto.getReceiverId());

        ChatMessage chatMessage = ChatMessage.builder()
                .chatRoomId(chatRoomId)
                .senderId(messageDto.getSenderId())
                .receiverId(messageDto.getReceiverId())
                .content(messageDto.getContent())
                .timestamp(LocalDateTime.now())
                .build();

        return chatMessageRepository.save(chatMessage);
    }

    private String generateChatRoomId(String userId1, String userId2) {
        return Stream.of(userId1, userId2).sorted().collect(Collectors.joining("_"));
    }
}

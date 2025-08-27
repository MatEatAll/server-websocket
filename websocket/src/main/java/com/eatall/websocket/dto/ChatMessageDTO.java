package com.eatall.websocket.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

//1:1 채팅방에서 사용

@Getter
@Setter
@Builder
public class ChatMessageDTO {
    private String senderId;      // 보내는 사람 ID
    private String receiverId;    // 받는 사람 ID
    private String content;       // 메시지 내용
    private LocalDateTime timestamp; // 보낸 시간
}
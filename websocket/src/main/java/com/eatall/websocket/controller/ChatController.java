package com.eatall.websocket.controller;

import com.eatall.websocket.document.ChatMessage;
import com.eatall.websocket.dto.ChatMessageDTO;
import com.eatall.websocket.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.stereotype.Controller;
import java.security.Principal;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatController {
    private final SimpUserRegistry simpUserRegistry; // 사용자 <-> 세션 맵
    private final SimpMessageSendingOperations messagingTemplate;
    private final ChatService chatService; // 메시지 저장 및 비즈니스 로직 처리
//    private final PushNotificationService pushNotificationService; // 푸시 알림 서비스

    // 1:1 메시지 처리
    @MessageMapping("/chat/private")
    public void handlePrivateMessage(SimpMessageHeaderAccessor accessor, ChatMessageDTO messageDto, Principal principal) {
        String senderId = principal.getName(); //STOMP 세션에서 현재 인증된 사용자의 id 가져오기
        messageDto.setSenderId(senderId);
        log.info("1:1 메시지 수신: 발신자={}, 수신자={}, 내용={}",
                messageDto.getSenderId(), messageDto.getReceiverId(), messageDto.getContent());

        ChatMessage chatMessage = chatService.saveMessage(messageDto);
        chatMessage.getChatRoomId();

        logAllConnectedUsers(); // 세션 맵 로그 출력

        // 특정 사용자에게 메시지 전송
        messagingTemplate.convertAndSendToUser(
                messageDto.getReceiverId(), // 수신자 ID
                "/queue/messages",             // 수신자가 구독하는 개인 큐
                chatMessage
        );
    }

    public void logAllConnectedUsers() {
        log.info("===== 현재 접속 중인 사용자-세션 맵 =====");
        // simpUserRegistry.getUsers()를 통해 현재 접속한 모든 SimpUser 객체를 가져옵니다.
        simpUserRegistry.getUsers().forEach(user -> {
            log.info("- 사용자: {}", user.getName());

            user.getSessions().forEach(session -> {
                log.info("  - 세션 ID: {}", session.getId());
            });
        });
        log.info("======================================");
    }
}
package com.eatall.websocket.document;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Document(collection = "chatMessages")
@Getter
@Builder
public class ChatMessage {
    @Id
    private String id;
    private String chatRoomId; // 1:1 채팅방을 구분하는 ID
    private String senderId;
    private String receiverId;
    private String content;
    private LocalDateTime timestamp;

    public void setSenderId(String senderId){
        this.senderId = senderId;
    }

    public void setTimestamp(LocalDateTime timestamp){
        this.timestamp = timestamp;
    }

    public void setChatRoomId(String chatRoomId){
        this.chatRoomId = chatRoomId;
    }
}
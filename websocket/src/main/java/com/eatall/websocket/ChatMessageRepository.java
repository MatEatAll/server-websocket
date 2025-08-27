package com.eatall.websocket;

import com.eatall.websocket.document.ChatMessage;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ChatMessageRepository extends MongoRepository<ChatMessage, String> {

    //채팅방 ID로 메시지를 시간순으로 찾기
    List<ChatMessage> findByChatRoomIdOrderByTimestampAsc(String chatRoomId);
}

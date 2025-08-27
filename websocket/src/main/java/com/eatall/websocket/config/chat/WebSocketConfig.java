package com.eatall.websocket.config.chat;

import com.eatall.websocket.config.jwt.JwtTokenValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Slf4j
@Configuration
@EnableWebSocketMessageBroker // STOMP 사용을 위한 어노테이션
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    private final JwtTokenValidator jwtTokenValidator; // 직접 구현한 JWT 프로바이더

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {

        registry.enableSimpleBroker("/topic","/queue");
        registry.setApplicationDestinationPrefixes("/pub");
        registry.setUserDestinationPrefix("/user");
    }

    // 클라이언트에서 웹소켓에 연결할 때 사용할 엔드포인트 설정 -> 최초 연결 시도할 때
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-stomp")
                .setAllowedOriginPatterns("*")
                .withSockJS();
        registry.addEndpoint("/ws-stomp") //sockJS 안쓰는 경우
                .setAllowedOriginPatterns("*");
    }

    //jwt 인증을 위한 인터셉터
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor =
                        MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

                // CONNECT 요청일 때 JWT 검증
                if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                    // 헤더에서 토큰 추출
                    String jwtToken = accessor.getFirstNativeHeader("Authorization");
                    if (jwtToken != null && jwtToken.startsWith("Bearer ")) {
                        String token = jwtToken.substring(7);
                        Authentication authentication = jwtTokenValidator.getAuthentication(token);
                        // SecurityContext에 저장하지 않고, STOMP 세션에 직접 Principal 설정
                        accessor.setUser(authentication);
                    }
                }
                return message;
            }
        });
    }
}
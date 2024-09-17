package com.websocketunit.service;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.websocketunit.entity.ChatMessage;
import lombok.AllArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class ChatService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    public ChatService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;

        // ObjectMapper에 JavaTimeModule 추가하여 Date, LocalDateTime 처리
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    // Redis에 메시지 저장
    public void saveMessage(String roomId, String sender, String message) {
        // roomId를 출력하여 확인
        System.out.println("Room ID: " + roomId);

        ChatMessage chatMessage = new ChatMessage(roomId, sender, message, new Date());
        // Redis에 저장할 때 키를 소문자로 통일
        redisTemplate.opsForList().rightPush("chatroom:" + roomId.toLowerCase(), chatMessage);

        // 저장된 메시지 출력
        System.out.println("Saved message: " + chatMessage);
    }

    // Redis에서 메시지 가져오기
    public List<ChatMessage> getMessagesByRoomId(String roomId) {
        List<Object> rawMessages = redisTemplate.opsForList().range("chatroom:" + roomId.toLowerCase(), 0, -1);

        // 가져온 데이터 로그로 확인
        System.out.println("Raw messages: " + rawMessages);

        return rawMessages.stream()
                .map(this::convertToChatMessage)  // 각 Object를 ChatMessage로 변환
                .collect(Collectors.toList());
    }


    // LinkedHashMap을 ChatMessage로 변환
    // LinkedHashMap을 ChatMessage로 변환
    private ChatMessage convertToChatMessage(Object object) {
        // object가 LinkedHashMap인지 확인
        if (object instanceof LinkedHashMap) {
            return objectMapper.convertValue(object, ChatMessage.class);
        } else {
            return (ChatMessage) object;
        }
    }


    // 클라이언트로 메시지 전송
    public void sendMessage(WebSocketSession session, String message) throws Exception {
        session.sendMessage(new TextMessage(message));
    }


    // 클라이언트에게 저장된 모든 메시지 전송
    public void sendPreviousMessages(WebSocketSession session, String roomId) throws Exception {
        List<ChatMessage> previousMessages = getMessagesByRoomId(roomId);

        for (ChatMessage msg : previousMessages) {
            // JSON 문자열로 변환
            String jsonMessage = objectMapper.writeValueAsString(msg);

            // JSON 형식으로 클라이언트에 전송
            sendMessage(session, jsonMessage);
        }
    }

}

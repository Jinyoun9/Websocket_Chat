package com.websocketunit.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.websocketunit.entity.ChatMessage;
import com.websocketunit.entity.ChatRoom;
import com.websocketunit.entity.ChatRoomManager;
import com.websocketunit.service.ChatService;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;


import java.util.*;

@Component

public class ChatWebSocketHandler extends TextWebSocketHandler {
    private final ChatService chatService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ChatWebSocketHandler(ChatService chatService) {
        this.chatService = chatService;
    }

    private String getRoomId(WebSocketSession session) {
        return (String) session.getAttributes().get("roomId");
    }


    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // 세션에서 roomId를 가져옴
        String roomId = getRoomId(session);

        // Redis에 저장된 이전 메시지들을 클라이언트에게 전송
        chatService.sendPreviousMessages(session, roomId);

        // 방에 세션 추가
        ChatRoom room = ChatRoomManager.getRoom(roomId);
        room.addSession(session);
    }

    public void broadcastMessage(String roomId, WebSocketSession senderSession, String message) throws Exception {
        ChatRoom room = ChatRoomManager.getRoom(roomId);

        for (WebSocketSession session : room.getSessions()) {
            if (!session.equals(senderSession)) {  // 보낸 클라이언트는 제외
                session.sendMessage(new TextMessage(message));
            }
        }
    }


      @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String roomId = getRoomId(session);
        Map<String, String> messageMap = objectMapper.readValue(message.getPayload(), Map.class);
        String sender = messageMap.get("sender");
        String userMessage = messageMap.get("message");

        // 메시지 저장 (예: Redis)
        chatService.saveMessage(roomId, sender, userMessage);

        // 채팅 메시지를 JSON으로 다시 변환하여 클라이언트로 전송
        ChatMessage chatMessage = new ChatMessage(roomId, sender, userMessage, new Date()); // timestamp 추가
        String broadcastMessage = objectMapper.writeValueAsString(chatMessage); // 모든 정보 포함

        // 자신을 제외한 모든 클라이언트에게 브로드캐스트
        broadcastMessage(roomId, session, broadcastMessage);
    }


    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception{
        String roomId = getRoomId(session);
        ChatRoom room = ChatRoomManager.getRoom(roomId);
        room.removeSession(session);
    }
}

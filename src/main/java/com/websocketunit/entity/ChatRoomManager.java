package com.websocketunit.entity;

import lombok.AllArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
public class ChatRoomManager {
    private static final Map<String, ChatRoom> rooms = new HashMap<>();

    public static ChatRoom getRoom(String roomId) {
        return rooms.computeIfAbsent(roomId, ChatRoom::new);
    }

    public static void removeRoom(String roomId){
        rooms.remove(roomId);
    }
}

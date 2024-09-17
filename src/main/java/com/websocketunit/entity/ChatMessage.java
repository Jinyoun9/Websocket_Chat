package com.websocketunit.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChatMessage implements Serializable {

    @JsonProperty("roomId")
    private String roomId;

    @JsonProperty("sender")
    private String sender;

    @JsonProperty("message")
    private String message;

    @JsonProperty("timestamp")
    private Date timestamp;

    public ChatMessage(String roomId, String sender, String message, Date timestamp) {
        this.roomId = roomId;
        this.sender = sender;
        this.message = message;
        this.timestamp = timestamp;
    }
}

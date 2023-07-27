package com.example.springbootwebsocket;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WebSocketController {
    @MessageMapping("/send")
    @SendTo("/websocket")
    public String sendMessage(String message) {
        return "received:" + message;
    }
}

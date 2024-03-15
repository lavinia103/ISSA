package com.example.carsharing;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final CarConnectionHandler carConnectionHandler;

    @Autowired
    public WebSocketConfig(CarConnectionHandler carConnectionHandler) {
        this.carConnectionHandler = carConnectionHandler;
    }
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(carConnectionHandler, "/car-connection");
    }
}
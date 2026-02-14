package com.example.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Standard WebSocket Configuration using STOMP.
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // /topic for Broadcast (1-to-many)
        // /queue for Private (1-to-1)
        config.enableSimpleBroker("/topic", "/queue");

        // Prefix for messages originating from the client
        config.setApplicationDestinationPrefixes("/app");

        // Prefix for private messages (SendToUser)
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOrigins("*") // In production, limit this to your frontend domain
                .withSockJS(); // Enables fallback options for older browsers
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // Add interceptors for security (e.g., JWT validation on CONNECT frames)
        // registration.interceptors(myAuthInterceptor);
    }
}

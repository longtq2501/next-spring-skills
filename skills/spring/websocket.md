# Skill: WebSocket & Real-time Communication

Guidelines for implementing robust, secure, and scalable real-time messaging using Spring Boot (STOMP) and Next.js.

## TL;DR - Quick Reference

### Critical Rules
1. **Use STOMP over WebSocket**: Standard sub-protocol for messaging with built-in routing (`@MessageMapping`).
2. **Security first**: Authenticate the initial WebSocket handshake using JWT in headers or query params.
3. **Heartbeats**: Enable heartbeats to detect and close dead connections promptly.
4. **Error Handling**: Use `@MessageExceptionHandler` to gracefully handle errors in message processing.
5. **Payload Size**: Keep WebSocket messages small; for large data, send a notification and fetch via REST.

---

## 1. Spring Boot Configuration

### STOMP Setup
Always separate the message broker into a "Simple Broker" (for dev/local) and a "Full Broker" (like RabbitMQ) for production scaling.

// Good: Basic STOMP Configuration
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/queue"); // Simple in-memory broker
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOrigins("*")
                .withSockJS(); // Fallback for browsers without WS support
    }
}

---

## 2. Message Handling

### Controller Pattern
Use `@MessageMapping` to handle incoming messages and `@SendTo` to broadcast output.

// Good: Real-time Chat Example
@Controller
public class ChatController {
    
    @MessageMapping("/chat.send")
    @SendTo("/topic/public")
    public ChatMessage sendMessage(@Payload ChatMessage message) {
        return message;
    }

    @MessageExceptionHandler
    @SendToUser("/queue/errors")
    public String handleException(Exception exception) {
        return "Error: " + exception.getMessage();
    }
}

---

## 3. Security (JWT & Interceptors)
Since WebSockets are long-lived, the initial handshake is the critical entry point, but STOMP frames also need individual validation.

### Handshake vs. Interceptors
- **Handshake Interceptor**: Used for the initial HTTP upgrade request.
- **Channel Interceptor**: Required for validating tokens on every `CONNECT` or `SUBSCRIBE` frame.

// Good: Registering a STOMP Interceptor
@Override
public void configureClientInboundChannel(ChannelRegistration registration) {
    registration.interceptors(new ChannelInterceptor() {
        @Override
        public Message<?> preSend(Message<?> message, MessageChannel channel) {
            StompHeaderAccessor accessor = MessageHeaderUtils.getAccessor(message, StompHeaderAccessor.class);
            if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                String token = accessor.getFirstNativeHeader("Authorization");
                // Validate token and set SecurityContext
            }
            return message;
        }
    });
}

---

## 4. Frontend Integration (Next.js)
Use libraries like `@stomp/stompjs` for robust STOMP client management.

// Good: Client-side Connection
const client = new Client({
  brokerURL: 'ws://localhost:8080/ws',
  onConnect: () => {
    client.subscribe('/topic/public', (message) => {
      console.log('Received:', JSON.parse(message.body));
    });
  },
});
client.activate();

---

## Related Skills
- **Security Config**: `skills/spring/security_config.md`
- **Performance Optimization**: `skills/spring/performance_optimization.md`

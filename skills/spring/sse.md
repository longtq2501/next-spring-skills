# Skill: Server-Sent Events (SSE)

Guidelines for implementing lightweight unidirectional server-to-client streaming.

## TL;DR - Quick Reference

### Critical Rules
1. **Unidirectional**: Use SSE for server-to-client push only (e.g., notifications, stock tickers). Use WebSocket for bi-directional.
2. **Standard Protocol**: SSE uses standard HTTP and doesn't require a special protocol upgrade (unlike WebSocket).
3. **Reconnection**: Browsers automatically reconnect to SSE streams if the connection drops.
4. **Content-Type**: Always set response to `text/event-stream`.
5. **Resource Cleaning**: Always complete or timeout `SseEmitter` to avoid thread/memory leaks.

---

## 1. Spring Boot Implementation

### SseEmitter (Imperative)
Ideal for basic notifications within a standard Spring Web project.

// Good: Basic SSE Controller
@GetMapping(path = "/notifications", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
public SseEmitter streamNotifications() {
    SseEmitter emitter = new SseEmitter(30_000L); // 30s timeout
    
    // In a real app, store emitter in a Map keyed by UserID
    executor.execute(() -> {
        try {
            emitter.send(SseEmitter.event()
                .name("message")
                .data("Hello at " + LocalTime.now()));
            emitter.complete();
        } catch (Exception e) {
            emitter.completeWithError(e);
        }
    });
    return emitter;
}

### WebFlux (Reactive)
Best for high-concurrency streaming.

// Good: Reactive Streaming
@GetMapping(path = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
public Flux<String> streamFlux() {
    return Flux.interval(Duration.ofSeconds(1))
               .map(i -> "Data chunk " + i);
}

---

## 2. Next.js Client Integration

### EventSource API
SSE is natively supported by the browser's `EventSource`.

// Good: Consuming SSE in React
useEffect(() => {
  const eventSource = new EventSource('/api/notifications');
  
  eventSource.onmessage = (event) => {
    const newData = JSON.parse(event.data);
    setNotifications(prev => [...prev, newData]);
  };

  eventSource.onerror = (err) => {
    console.error("SSE failed:", err);
    eventSource.close();
  };

  return () => eventSource.close(); // Important: Cleanup
}, []);

---

## 3. Advanced Patterns

### Last-Event-ID
Used for data consistency during reconnections.
- The server sends an `id` with each event.
- If the client reconnects, it sends the `Last-Event-ID` header.

### Scaling SSE
- **Load Balancers**: Ensure your load balancer supports long-lived connections and `text/event-stream` (disable buffering).
- **Redis Pub/Sub**: Use Redis to broadcast events across multiple server instances.

---

## Related Skills
- **WebSocket & STOMP**: `skills/spring/websocket.md`
- **Performance Optimization**: `skills/spring/performance_optimization.md`

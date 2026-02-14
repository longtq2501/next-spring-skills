# Skill: WebRTC - Peer-to-Peer Communication

Guidelines for implementing real-time video, audio, and data streaming between browsers.

## TL;DR - Quick Reference

### Critical Rules
1. **Signaling Server Required**: WebRTC needs a side-channel (WebSocket/SSE) to exchange SDP and ICE candidates.
2. **ICE Protocol**: Use STUN/TURN servers to bypass NAT and Firewalls. (Google's STUN is for dev, use Twilio/Metered for Prod).
3. **Media Constraints**: Use `navigator.mediaDevices.getUserMedia` with specific resolutions to optimize performance.
4. **DataChannels**: Use `RTCDataChannel` for low-latency P2P data (e.g., file sharing, gaming).
5. **Security**: Always use HTTPS. WebRTC will not work on non-secure origins.

---

## 1. Core Lifecycle (Signaling)

WebRTC communication happens in steps:
1. **Offer**: Peer A creates an offer (SDP).
2. **Answer**: Peer B receives the offer and sends back an answer.
3. **ICE Candidates**: Both peers exchange network pathway information.

// Bad: Trying to connect without a signaling server
// Good: Using WebSocket for signaling
socket.on('offer', async (offer) => {
  await peerConnection.setRemoteDescription(new RTCSessionDescription(offer));
  const answer = await peerConnection.createAnswer();
  await peerConnection.setLocalDescription(answer);
  socket.emit('answer', answer);
});

---

## 2. Media Handling

### Capturing Video/Audio
// Good: Requesting specific constraints
const stream = await navigator.mediaDevices.getUserMedia({
  video: { width: 1280, height: 720 },
  audio: true
});
videoRef.current.srcObject = stream;

---

## 3. NAT Traversal (STUN/TURN)
Without these, peers cannot find each other over the public internet.

// Good: Configuration with ICE servers
const pcConfig = {
  iceServers: [
    { urls: 'stun:stun.l.google.com:19302' }, // STUN finds public IP
    { 
      urls: 'turn:my-turn-server.com', // TURN relays data if P2P fails
      username: 'user', 
      credential: 'password' 
    }
  ]
};
const peerConnection = new RTCPeerConnection(pcConfig);

---

## 4. Performance & Troubleshooting

### Optimization
- **Simulcast**: Send multiple resolution streams to cater to different bandwidths.
- **Bitrate Capping**: Manually limit bandwidth to prevent congestion.

### Debugging
- **chrome://webrtc-internals**: Essential tool for inspecting connections and bitrates.

---

## Related Skills
- **WebSocket & STOMP**: `skills/spring/websocket.md`
- **Interactivity & Animation**: `skills/nextjs/interactivity.md`

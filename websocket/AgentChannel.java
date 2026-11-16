@Component
public class AgentChannel {
    private final Map<UUID, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final Map<String, CompletableFuture<String>> inflight = new ConcurrentHashMap<>();

    public CompletableFuture<String> dispatch(Device d, AgentRequest req) {
        WebSocketSession s = sessions.get(d.getId());
        if (s == null || !s.isOpen()) throw new Conflict("agent_offline");
        String id = UUID.randomUUID().toString();
        var future = new CompletableFuture<String>();
        inflight.put(id, future);
        send(s, Map.of("type", "exec", "id", id, "verb", req.verb(), "args", req.args()));
        return future;
    }

    public void onResult(String id, String output) {
        var f = inflight.remove(id);
        if (f != null) f.complete(output);
    }

    public void register(UUID deviceId, WebSocketSession s) { sessions.put(deviceId, s); }
    public void drop(UUID deviceId) { sessions.remove(deviceId); inflight.values().forEach(f -> f.cancel(true)); }
}

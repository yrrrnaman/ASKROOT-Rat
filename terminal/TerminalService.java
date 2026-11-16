@Service
public class TerminalService {
    private final CommandRegistry registry;
    private final DeviceRepository devices;
    private final ExecutionRepository executions;
    private final AgentChannel channel;
    private final AuditService audit;
    private final AskRootProperties props;

    @Transactional
    public ExecutionResponse execute(UUID deviceId, String raw, User actor) {
        var device = devices.findById(deviceId).orElseThrow(() -> new NotFound("device"));
        if (device.getStatus() != Device.Status.ONLINE)
            throw new Conflict("device_not_online");

        String[] parts = raw.trim().split("\\s+");
        CommandHandler handler = registry.resolve(parts[0]);
        List<String> args = List.of(parts).subList(1, parts.length);
        registry.validate(handler, args);

        if (handler.scope() == CommandHandler.Scope.ELEVATED && actor.getRole() == Role.READONLY)
            throw new Forbidden("elevation_required");

        var row = executions.save(Execution.pending(device, actor, handler.verb(), String.join(" ", args)));
        audit.record(actor, device, "command.execute", "attempt", Map.of("verb", handler.verb()));

        long start = System.nanoTime();
        try {
            String output = channel.dispatch(device, new AgentRequest(handler.verb(), args))
                                  .get(props.getAgent().getCommandTimeout().toMillis(), TimeUnit.MILLISECONDS);
            row.complete("OK", 0, truncate(output), ms(start));
        } catch (TimeoutException e) {
            row.complete("TIMEOUT", null, "agent did not answer within timeout", ms(start));
        } catch (Exception e) {
            row.complete("ERROR", 1, e.getMessage(), ms(start));
        }
        audit.record(actor, device, "command.execute", row.getStatus().name(), Map.of("verb", handler.verb()));
        return ExecutionResponse.from(row);
    }

    private String truncate(String s) {
        int max = props.getAgent().getMaxOutputBytes();
        return s.length() > max ? s.substring(0, max) + "\n[truncated]" : s;
    }
}

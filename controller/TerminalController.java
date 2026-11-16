@RestController @RequestMapping("/api/terminal")
public class TerminalController {
    private final TerminalService terminal;
    private final ExecutionRepository executions;

    @PostMapping("/execute")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR')")
    public ExecutionResponse execute(@Valid @RequestBody ExecuteRequest req, @AuthenticationPrincipal User me) {
        return terminal.execute(req.deviceId(), req.command(), me);
    }

    @GetMapping("/history")
    public Page<ExecutionResponse> history(@RequestParam(required = false) UUID deviceId, Pageable page) {
        return (deviceId == null ? executions.findAllByOrderByCreatedAtDesc(page)
                                 : executions.findByDeviceIdOrderByCreatedAtDesc(deviceId, page))
                .map(ExecutionResponse::from);
    }

    @GetMapping("/commands")
    public Set<String> commands() { return terminal.verbs(); }
}

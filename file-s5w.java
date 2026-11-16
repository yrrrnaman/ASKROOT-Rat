@RestController @RequestMapping("/api/devices")
public class DeviceController {
    private final DeviceService devices;

    @GetMapping
    public Page<DeviceDto> list(@RequestParam(required = false) Device.Status status, Pageable page) {
        return devices.list(status, page);
    }

    @GetMapping("/{id}")
    public DeviceDto get(@PathVariable UUID id) { return devices.one(id); }

    @PostMapping("/enroll")
    @PreAuthorize("hasRole('ADMIN')")
    public EnrollmentResponse enroll(@AuthenticationPrincipal User me) { return devices.issueCode(me); }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public DeviceDto approve(@PathVariable UUID id, @AuthenticationPrincipal User me) { return devices.approve(id, me); }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR')")
    public DeviceDto rename(@PathVariable UUID id, @Valid @RequestBody RenameRequest r) { return devices.rename(id, r.hostname()); }

    @PostMapping("/{id}/disconnect")
    public void disconnect(@PathVariable UUID id) { devices.disconnect(id); }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void revoke(@PathVariable UUID id, @AuthenticationPrincipal User me) { devices.revoke(id, me); }
}
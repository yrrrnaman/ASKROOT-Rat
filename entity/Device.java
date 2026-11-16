package com.askroot.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Entity @Table(name = "devices")
@Getter @Setter @NoArgsConstructor
public class Device {
    @Id @GeneratedValue private UUID id;
    @Column(nullable = false) private String hostname;
    @Column(nullable = false) private String os;
    @Column(nullable = false) private String arch;
    @Column(name = "ip_address") private String ipAddress;
    @Column(name = "agent_version") private String agentVersion;
    @Column(nullable = false, unique = true) private String fingerprint;
    @Column(name = "credential_hash") private String credentialHash;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private Status status = Status.PENDING_APPROVAL;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "owner_id", nullable = false) private User owner;
    @Column(name = "approved_at") private Instant approvedAt;
    @Column(name = "last_seen_at") private Instant lastSeenAt;
    @Column(name = "created_at") private Instant createdAt = Instant.now();

    public enum Status { PENDING_APPROVAL, ONLINE, OFFLINE, REVOKED }

    @Transient public boolean isManageable() { return status == Status.ONLINE; }
}

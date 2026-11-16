@Transactional(propagation = Propagation.REQUIRES_NEW)
public void record(User actor, Device device, String action, String result, Map<String, Object> detail) {
    String prev = repo.lastHash();
    String payload = actor.getId() + "|" + action + "|" + result + "|" + detail + "|" + Instant.now();
    String hash = sha256(prev + payload);
    repo.save(AuditLog.builder().actor(actor).device(device).action(action).result(result)
        .detail(toJson(detail)).prevHash(prev).hash(hash).actorIp(currentIp()).build());
}

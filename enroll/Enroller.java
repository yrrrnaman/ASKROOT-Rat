public void enroll(String server, String code) throws Exception {
    String fingerprint = sha256(Files.readString(Path.of("/etc/machine-id")) + hostname());
    var body = objectMapper.writeValueAsString(Map.of(
        "code", code, "hostname", hostname(), "os", osName(), "arch", osArch(), "fingerprint", fingerprint));
    var res = http.send(post(server + "/api/agent/enroll", body));
    if (res.statusCode() != 200) throw new IllegalStateException("enrollment rejected: " + res.body());

    var cred = objectMapper.readTree(res.body());
    Files.writeString(stateFile(), objectMapper.writeValueAsString(Map.of(
        "deviceId", cred.get("deviceId").asText(),
        "secret", cred.get("secret").asText(),
        "server", server)), OWNER_ONLY);
}

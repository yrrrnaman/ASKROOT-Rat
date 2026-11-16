public void run() throws Exception {
    while (true) {
        try (var ws = client.newWebSocketBuilder()
                .header("X-Device-Id", cred.deviceId())
                .header("X-Device-Signature", hmac(cred.secret(), cred.deviceId()))
                .buildAsync(URI.create(server.replace("https", "wss") + "/agent-ws"), listener).join()) {
            this.session = ws;
            scheduleHeartbeat();
            latch.await();
        } catch (CompletionException e) {
            if (e.getCause() instanceof RevokedException) return;
        }
        Thread.sleep(backoff.next());
    }
}

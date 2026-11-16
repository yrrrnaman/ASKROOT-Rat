public String run(String verb, List<String> args) throws Exception {
    return switch (verb) {
        case "system-info"  -> sysInfo();
        case "cpu-status"   -> cpu();
        case "memory-status"-> memInfo();
        case "disk-status"  -> exec("df", "-h", "--output=source,size,used,avail,pcent");
        case "network-status" -> exec("ip", "-br", "addr");
        case "process-list" -> exec("ps", "-eo", "pid,pcpu,pmem,comm", "--sort=-pcpu");
        case "service-status" -> exec("systemctl", "list-units", "--type=service", "--no-pager", "--no-legend");
        case "uptime"       -> exec("uptime", "-p");
        case "file-list"    -> list(args.isEmpty() ? "." : args.get(0));
        default -> throw new IllegalArgumentException("unsupported: " + verb);
    };
}

private String exec(String... argv) throws Exception {
    var p = new ProcessBuilder(argv).redirectErrorStream(true).start();
    if (!p.waitFor(15, TimeUnit.SECONDS)) { p.destroyForcibly(); return "[timeout]"; }
    return new String(p.getInputStream().readAllBytes(), UTF_8);
}

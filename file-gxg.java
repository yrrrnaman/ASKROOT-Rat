@Component
public class DownloadFileHandler implements CommandHandler {
    public String verb() { return "download-file"; }
    public Scope scope() { return Scope.ELEVATED; }
    public int minArgs() { return 1; } public int maxArgs() { return 1; }
    public Pattern argPattern() { return Pattern.compile("^[A-Za-z0-9._/-]{1,255}$"); }
    public String run(AgentRequest ctx) { return ctx.execute("download-file", ctx.args().get(0)); }
}
@Component
public class SystemInfoHandler implements CommandHandler {
    public String verb() { return "system-info"; }
    public Scope scope() { return Scope.BASIC; }
    public int minArgs() { return 0; } public int maxArgs() { return 0; }
    public Pattern argPattern() { return null; }
    public String run(AgentRequest ctx) { return ctx.execute("system-info"); }
}
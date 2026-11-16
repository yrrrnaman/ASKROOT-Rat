package com.askroot.terminal;

import java.util.List;

public interface CommandHandler {
    String verb();
    Scope scope();
    int minArgs();
    int maxArgs();
    java.util.regex.Pattern argPattern();      // null = no args
    String run(AgentRequest ctx) throws Exception;

    enum Scope { BASIC, ELEVATED }
}


package com.askroot.terminal;

import org.springframework.stereotype.Component;
import java.util.*;
import java.util.regex.Pattern;

@Component
public class CommandRegistry {
    private static final Pattern SAFE_PATH = Pattern.compile("^[A-Za-z0-9._/-]{1,255}$");
    private final Map<String, CommandHandler> handlers = new LinkedHashMap<>();

    public CommandRegistry(List<CommandHandler> all) {
        all.forEach(h -> handlers.put(h.verb(), h));
    }

    public CommandHandler resolve(String verb) {
        CommandHandler h = handlers.get(verb);
        if (h == null) throw new IllegalArgumentException("unknown_verb:" + verb);
        return h;
    }

    public void validate(CommandHandler h, List<String> args) {
        if (args.size() < h.minArgs() || args.size() > h.maxArgs())
            throw new IllegalArgumentException("arity:" + h.verb());
        if (h.argPattern() != null) {
            for (String a : args) {
                if (!h.argPattern().matcher(a).matches())
                    throw new IllegalArgumentException("arg:" + a);
                if (a.contains("..")) throw new IllegalArgumentException("traversal:" + a);
            }
        }
    }

    public Set<String> verbs() { return Collections.unmodifiableSet(handlers.keySet()); }
}

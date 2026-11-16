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

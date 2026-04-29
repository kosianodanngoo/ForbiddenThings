package io.github.kosianodangoo.forbiddenthings.agent;

import java.lang.instrument.Instrumentation;
import java.util.logging.Logger;

public class ForbiddenAgent {
    private static final Logger LOGGER = Logger.getLogger("ForbiddenAgent");
    public static volatile Instrumentation INSTRUMENTATION;

    static {
        LOGGER.info("Agent Static");
    }

    public static void premain(String args, Instrumentation inst) {
        INSTRUMENTATION = inst;
        LOGGER.info("Premain");
    }

    public static void agentmain(String args, Instrumentation inst) {
        INSTRUMENTATION = inst;
        LOGGER.info("Agentmain");
    }
}

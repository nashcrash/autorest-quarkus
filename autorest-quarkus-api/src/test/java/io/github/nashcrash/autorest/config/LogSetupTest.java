package io.github.nashcrash.autorest.config;

public class LogSetupTest {
    static {
        System.setProperty("java.util.logging.manager", "org.jboss.logmanager.LogManager");
    }
}

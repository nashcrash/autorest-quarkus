package io.github.nashcrash.autorest.common.context;

import org.eclipse.microprofile.context.spi.ThreadContextProvider;
import org.eclipse.microprofile.context.spi.ThreadContextSnapshot;

import java.util.Map;

public class ContextManagerProvider implements ThreadContextProvider {
    @Override
    public ThreadContextSnapshot currentContext(Map<String, String> map) {
        // Take a snapshot of the current ThreadLocal state
        final Map<String, String> capturedState = ContextManager.getAllContext();

        return () -> {
            //BACKUP: Save what's on the target thread before overwriting
            final Map<String, String> backup = ContextManager.getAllContext();

            //RESTORE: Inject the captured state into the new thread
            ContextManager.setAllContext(capturedState);

            //CLEANUP: Return the closure to revert the thread state later
            return () -> {
                try {
                    ContextManager.setAllContext(backup);
                } catch (Exception e) {
                    ContextManager.removeContext(); // Safety net
                }
            };
        };
    }

    @Override
    public ThreadContextSnapshot clearedContext(Map<String, String> map) {
        // This is used when the framework needs a "clean" thread without any context
        return () -> {
            final Map<String, String> backup = ContextManager.getAllContext();
            ContextManager.removeContext();
            return () -> ContextManager.setAllContext(backup);
        };
    }

    @Override
    public String getThreadContextType() {
        return "ContextManager";
    }
}

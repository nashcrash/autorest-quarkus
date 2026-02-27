package io.github.nashcrash.autorest.common.context;

import java.util.LinkedHashMap;
import java.util.Map;

public class ContextManager {
    private static final ThreadLocal<Map<String, String>> context = new InheritableThreadLocal<>();

    private static void init() {
        if (context.get() == null) {
            context.set(new LinkedHashMap<>());
        }
    }

    public static void removeContext() {
        context.remove();
    }

    public static boolean isEmpty() {
        init();
        return context.get().isEmpty();
    }

    public static String getParameter(String key) {
        init();
        return context.get().get(key);
    }

    public static void setParameter(String key, String value) {
        init();
        context.get().put(key, value);
    }

    public static Map<String, String> getAllContext() {
        init();
        return context.get();
    }
}
package io.github.nashcrash.autorest.common.context;

import jakarta.enterprise.context.RequestScoped;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@RequestScoped
public class ContextBean {
    private final Map<String, String> data = new HashMap<>();

    public boolean isEmpty() {
        return data.isEmpty();
    }

    public String get(String key) {
        return data.get(key);
    }

    public void set(String key, String value) {
        data.put(key, value);
    }
    public String getOrDefault(String key, String defaultValue) {
        return Objects.toString(data.get(key), defaultValue);
    }
    public void setIfAbsent(String key, String value) {
        data.putIfAbsent(key, value);
    }

    public Map<String, String> getAll() {
        return data;
    }

    public void setAll(Map<String, String> map) {
        data.clear();
        data.putAll(map);
    }
}
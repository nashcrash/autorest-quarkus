import io.quarkus.test.junit.QuarkusTestProfile;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Read application-test.properties as test configuration file.
 * Overwrite this class to configure a different profile or test configuration file,
 * simply using one of the proposed constructors.
 */
public class TestCustomProfile implements QuarkusTestProfile {
    protected List<String> configProfiles;
    protected List<String> filenames;
    protected Type type;

    public TestCustomProfile() {
        this("test");
    }

    protected TestCustomProfile(String... configProfile) {
        this(Arrays.stream(configProfile).toList(), Arrays.stream(configProfile).map(c -> "application-" + c + ".yaml").toList(), Type.YAML);
    }

    protected TestCustomProfile(List<String> configProfiles, List<String> filenames, Type type) {
        this.configProfiles = configProfiles;
        this.filenames = filenames;
        this.type = type;
    }

    protected TestCustomProfile(String configProfile, String filename, Type type) {
        this.configProfiles = new ArrayList<>();
        this.configProfiles.add(configProfile);
        this.filenames = new ArrayList<>();
        this.filenames.add(filename);
        this.type = type;
    }

    @Override
    public String getConfigProfile() {
        return this.configProfiles.get(0);
    }

    @Override
    public Map<String, String> getConfigOverrides() {
        Map<String, String> map = new HashMap<>();
        for (String filename : this.filenames) {
            if (Type.PROPERTIES.equals(type)) {
                map.putAll(loadProperties(filename));
            } else if (Type.YAML.equals(type)) {
                map.putAll(loadYaml(filename));
            } else {
                throw new IllegalArgumentException("Invalid type for profiled application config file.");
            }
        }
        return map;
    }

    private Map<String, String> loadYaml(String fileName) {
        Yaml yaml = new Yaml();
        try (InputStream input = Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName)) {
            if (input == null) {
                throw new RuntimeException("File YAML not found: " + fileName);
            }
            Map<String, String> flatMap = new HashMap<>();
            Map<String, Object> source = yaml.load(input);
            resolveVariables(source, source);
            flattenMap("", source, flatMap);
            return flatMap;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Map<String, String> loadProperties(String fileName) {
        Map<String, String> configMap = new HashMap<>();
        Properties properties = new Properties();

        try (InputStream input = Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName)) {
            if (input == null) {
                throw new IOException("File not found: " + fileName);
            }
            properties.load(input);

            for (String key : properties.stringPropertyNames()) {
                configMap.put(key, properties.getProperty(key));
            }

        } catch (IOException e) {
            throw new RuntimeException("Configuration file error", e);
        }

        return configMap;
    }

    private static void flattenMap(String prefix, Map<String, Object> source, Map<String, String> target) {
        for (Map.Entry<String, Object> entry : source.entrySet()) {
            String key = prefix.isEmpty() ? entry.getKey() : prefix + "." + entry.getKey();

            if (entry.getValue() instanceof Map) {
                flattenMap(key, (Map<String, Object>) entry.getValue(), target);
            } else if (entry.getValue() instanceof Collection) {
                int i = 0;
                for (Object obj : (Collection) entry.getValue()) {
                    if (obj instanceof Map<?, ?>) {
                        Map<String, Object> element = (Map<String, Object>) obj;
                        flattenMap(key + "[" + i + "]", element, target);
                    } else {
                        target.put(key + "[" + i + "]", entry.getValue().toString());
                    }
                    i++;
                }
            } else {
                target.put(key, entry.getValue().toString());
            }
        }
    }

    private static void resolveVariables(Map<String, Object> origin, Map<String, Object> data) {
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            if (entry.getValue() instanceof String) {
                String value = (String) entry.getValue();
                if (value.startsWith("${") && value.endsWith("}")) {
                    String key = value.substring(2, value.length() - 1);
                    Object replacement = resolvePath(origin, key);
                    if (replacement != null) {
                        entry.setValue(replacement);
                    }
                }
            } else if (entry.getValue() instanceof Collection) {
                List replace = new ArrayList();
                for (Object element : (Collection) entry.getValue()) {
                    if (element instanceof Map) {
                        resolveVariables(origin, (Map<String, Object>) element);
                        replace.add(element);
                    } else if (element instanceof String) {
                        String value = (String) element;
                        if (value.startsWith("${") && value.endsWith("}")) {
                            String key = value.substring(2, value.length() - 1);
                            Object replacement = resolvePath(origin, key);
                            if (replacement != null) {
                                replace.add(replacement);
                            } else {
                                replace.add(element);
                            }
                        } else {
                            replace.add(element);
                        }
                    }
                }
                entry.setValue(replace);
            } else if (entry.getValue() instanceof Map) {
                resolveVariables(origin, (Map<String, Object>) entry.getValue());
            }
        }
    }

    private static Object resolvePath(Map<String, Object> origin, String key) {
        Object element = null;
        Map<String, Object> inner = origin;
        String[] keys = key.split("\\.");
        for (int i = 0; i < keys.length; i++) {
            Matcher matcher = Pattern.compile("([^\\[]+)(\\[([0-9]+)\\])?").matcher(keys[i]);
            if (matcher.matches()) {
                String fieldName = matcher.group(1);
                element = inner.get(fieldName);
                if (element == null) break;
                if (element instanceof List) {
                    if (matcher.group(3) != null) {
                        int pos = Integer.parseInt(matcher.group(3));
                        element = ((List) element).get(pos);
                    }
                }
                if (element instanceof Map<?, ?>) {
                    inner = (Map<String, Object>) element;
                }
            }
        }
        return element;
    }

    public enum Type {
        YAML,
        PROPERTIES
    }
}

package io.github.nashcrash.autorest.testengine;

import io.quarkus.test.junit.QuarkusTestProfile;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class EngineTestProfile implements QuarkusTestProfile {

    private static final String PROFILE = "engine-test";
    private static final String PREFIX = "application-" + PROFILE;
    private static final String EXTENSION = ".yaml";

    @Override
    public String getConfigProfile() {
        return PROFILE;
    }

    @Override
    public Map<String, String> getConfigOverrides() {
        List<String> locations = discoverYamlFragments();

        if (locations.isEmpty()) {
            throw new IllegalStateException(
                    "No application-engine-test*.yaml files found on test classpath"
            );
        }

        return Map.of(
                "quarkus.config.locations",
                String.join(",", locations)
        );
    }

    /**
     * Discovers all application-engine-test*.yaml files on the test classpath,
     * excluding the base application-engine-test.yaml file.
     */
    private List<String> discoverYamlFragments() {
        try {
            Enumeration<URL> roots =
                    Thread.currentThread()
                            .getContextClassLoader()
                            .getResources("");

            List<String> result = new ArrayList<>();

            while (roots.hasMoreElements()) {
                URL root = roots.nextElement();
                Path rootPath = toPath(root);

                if (rootPath != null && Files.isDirectory(rootPath)) {
                    try (Stream<Path> files = Files.list(rootPath)) {
                        result.addAll(
                                files
                                        .filter(Files::isRegularFile)
                                        .map(Path::getFileName)
                                        .map(Path::toString)
                                        .filter(this::isFragmentYaml)
                                        .toList()
                        );
                    }
                }
            }

            return result.stream()
                    .sorted((a, b) -> {
                        if (a.equals(PREFIX + EXTENSION)) return -1;
                        if (b.equals(PREFIX + EXTENSION)) return 1;
                        return a.compareTo(b);
                    })
                    .toList();

        } catch (IOException e) {
            throw new RuntimeException("Failed to scan classpath for YAML config fragments", e);
        }
    }

    private boolean isFragmentYaml(String filename) {
        return filename.startsWith(PREFIX)
                && filename.endsWith(EXTENSION);
    }

    private Path toPath(URL url) {
        try {
            URI uri = url.toURI();
            if ("file".equals(uri.getScheme())) {
                return Path.of(uri);
            }
        } catch (URISyntaxException ignored) {
        }
        return null;
    }
}

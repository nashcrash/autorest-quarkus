package io.github.nashcrash.autorest.testengine;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;

/**
 * Scans the runtime classpath (no external dependencies) looking for interfaces that
 * extend {@link TestCasesProperties} and are annotated with {@link TestEngineProperties}.
 * <p>
 * Both directory roots (e.g. {@code target/test-classes} of the project under test) and
 * jar entries are inspected, so discovery keeps working when {@code autorest-quarkus-test}
 * is used as a library in a consumer project.
 * <p>
 * To keep the scan cheap, each {@code .class} file is first filtered by checking whether its
 * constant pool contains the annotation descriptor; only matching candidates are then loaded
 * and confirmed via reflection. An optional narrowing package prefix can be provided through
 * the {@value #SCAN_PACKAGE_PROPERTY} system property.
 */
public final class ClasspathTestPropertiesScanner {

    /** Optional system property to restrict the scan to a single base package (by class name prefix). */
    public static final String SCAN_PACKAGE_PROPERTY = "autorest.testengine.scan-package";

    private static final String CLASS_SUFFIX = ".class";

    private final ClassLoader classLoader;
    private final String basePackage;
    private final byte[] annotationDescriptor;

    private ClasspathTestPropertiesScanner(ClassLoader classLoader) {
        this.classLoader = classLoader;
        String prop = System.getProperty(SCAN_PACKAGE_PROPERTY, "");
        this.basePackage = prop == null ? "" : prop.trim();
        this.annotationDescriptor = ("L" + TestEngineProperties.class.getName().replace('.', '/') + ";")
                .getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Discovers all annotated {@link TestCasesProperties} mappings on the current classpath.
     *
     * @param classLoader the class loader used to resolve candidate classes; when {@code null}
     *                    the current thread context class loader is used
     * @return the discovered mapping classes, sorted by fully qualified name (deterministic)
     */
    public static List<Class<? extends TestCasesProperties>> discover(ClassLoader classLoader) {
        ClassLoader cl = classLoader != null ? classLoader : Thread.currentThread().getContextClassLoader();
        if (cl == null) {
            cl = ClasspathTestPropertiesScanner.class.getClassLoader();
        }
        return new ClasspathTestPropertiesScanner(cl).scan();
    }

    private List<Class<? extends TestCasesProperties>> scan() {
        Set<Class<? extends TestCasesProperties>> found = new LinkedHashSet<>();
        for (File root : classpathRoots()) {
            try {
                if (root.isDirectory()) {
                    scanDirectory(root, found);
                } else if (root.isFile() && root.getName().toLowerCase().endsWith(".jar")) {
                    scanJar(root, found);
                }
            } catch (Exception ignored) {
                // A single unreadable classpath entry must not break discovery.
            }
        }
        List<Class<? extends TestCasesProperties>> result = new ArrayList<>(found);
        result.sort(Comparator.comparing(Class::getName));
        return result;
    }

    /** Collects classpath entries from the class loader hierarchy and the {@code java.class.path} property. */
    private Set<File> classpathRoots() {
        Set<File> roots = new LinkedHashSet<>();
        for (ClassLoader cl = classLoader; cl != null; cl = cl.getParent()) {
            if (cl instanceof URLClassLoader) {
                for (URL url : ((URLClassLoader) cl).getURLs()) {
                    addRoot(roots, url);
                }
            }
        }
        String classPath = System.getProperty("java.class.path", "");
        for (String entry : classPath.split(File.pathSeparator)) {
            if (!entry.isEmpty()) {
                roots.add(new File(entry));
            }
        }
        return roots;
    }

    private void addRoot(Set<File> roots, URL url) {
        if (!"file".equals(url.getProtocol())) {
            return;
        }
        try {
            roots.add(new File(url.toURI()));
        } catch (Exception e) {
            roots.add(new File(url.getPath()));
        }
    }

    private void scanDirectory(File root, Set<Class<? extends TestCasesProperties>> found) throws IOException {
        Path rootPath = root.toPath();
        try (Stream<Path> stream = Files.walk(rootPath)) {
            stream.filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().endsWith(CLASS_SUFFIX))
                    .forEach(p -> {
                        String className = toClassName(rootPath.relativize(p).toString());
                        if (!matchesBasePackage(className)) {
                            return;
                        }
                        try {
                            byte[] bytes = Files.readAllBytes(p);
                            inspect(className, bytes, found);
                        } catch (IOException ignored) {
                            // Skip unreadable class file.
                        }
                    });
        }
    }

    private void scanJar(File jar, Set<Class<? extends TestCasesProperties>> found) throws IOException {
        try (JarFile jarFile = new JarFile(jar)) {
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (entry.isDirectory() || !entry.getName().endsWith(CLASS_SUFFIX)) {
                    continue;
                }
                String className = toClassName(entry.getName());
                if (!matchesBasePackage(className)) {
                    continue;
                }
                try (InputStream in = jarFile.getInputStream(entry)) {
                    inspect(className, in.readAllBytes(), found);
                } catch (IOException ignored) {
                    // Skip unreadable jar entry.
                }
            }
        }
    }

    private void inspect(String className, byte[] bytes, Set<Class<? extends TestCasesProperties>> found) {
        if (!containsAnnotationDescriptor(bytes)) {
            return;
        }
        try {
            Class<?> clazz = Class.forName(className, false, classLoader);
            if (clazz.isInterface()
                    && TestCasesProperties.class.isAssignableFrom(clazz)
                    && clazz.isAnnotationPresent(TestEngineProperties.class)) {
                found.add(clazz.asSubclass(TestCasesProperties.class));
            }
        } catch (Throwable ignored) {
            // Not loadable / not resolvable in this context: skip.
        }
    }

    private boolean matchesBasePackage(String className) {
        return basePackage.isEmpty() || className.startsWith(basePackage);
    }

    private static String toClassName(String path) {
        String normalized = path.replace('\\', '/');
        normalized = normalized.substring(0, normalized.length() - CLASS_SUFFIX.length());
        return normalized.replace('/', '.');
    }

    /** Cheap constant-pool substring check for the annotation type descriptor. */
    private boolean containsAnnotationDescriptor(byte[] haystack) {
        byte[] needle = annotationDescriptor;
        if (needle.length == 0 || haystack.length < needle.length) {
            return false;
        }
        int last = haystack.length - needle.length;
        outer:
        for (int i = 0; i <= last; i++) {
            for (int j = 0; j < needle.length; j++) {
                if (haystack[i + j] != needle[j]) {
                    continue outer;
                }
            }
            return true;
        }
        return false;
    }
}

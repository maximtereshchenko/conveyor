package com.github.maximtereshchenko.conveyor.domain.test;

import javax.tools.*;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

final class JarBuilder {

    private static final Pattern INTERPOLATION_PATTERN = Pattern.compile("\\$\\{([^}]+)}");

    private final Path templateDirectory;
    private final Map<String, String> values;

    private JarBuilder(Path templateDirectory, Map<String, String> values) {
        this.templateDirectory = templateDirectory;
        this.values = Map.copyOf(values);
    }

    static JarBuilder from(String templateDirectory) {
        return new JarBuilder(path(templateDirectory), Map.of());
    }

    private static Path path(String templateDirectory) {
        try {
            return Paths.get(
                Objects.requireNonNull(
                        Thread.currentThread()
                            .getContextClassLoader()
                            .getResource(templateDirectory)
                    )
                    .toURI()
            );
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    JarBuilder name(String name) {
        return with("name", name)
            .with("normalizedName", name.replaceAll("[-:.]", ""));
    }

    JarBuilder version(String version) {
        return with("version", String.valueOf(version));
    }

    byte[] bytes() {
        var byteArrayOutputStream = new ByteArrayOutputStream();
        try (var zipOutputStream = new ZipOutputStream(byteArrayOutputStream)) {
            for (var fileObject : compiled()) {
                zipOutputStream.putNextEntry(new ZipEntry(fileObject.toUri().toString()));
                try (var inputStream = fileObject.openInputStream()) {
                    inputStream.transferTo(zipOutputStream);
                }
                zipOutputStream.closeEntry();
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return byteArrayOutputStream.toByteArray();
    }

    void install(Path path) {
        try {
            Files.write(path.resolve(fileName()), bytes());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private JarBuilder with(String key, String value) {
        var copy = new HashMap<>(values);
        copy.put(key, value);
        return new JarBuilder(templateDirectory, copy);
    }

    private String fileName() {
        return "%s-%s.jar".formatted(values.get("name"), values.get("version"));
    }

    private String interpolated(String original) {
        return INTERPOLATION_PATTERN.matcher(original)
            .results()
            .reduce(
                original,
                (current, matchResult) ->
                    current.replace(matchResult.group(), values.get(matchResult.group(1))),
                (first, second) -> first
            );
    }

    private Collection<FileObject> compiled() throws IOException {
        var compiler = ToolProvider.getSystemJavaCompiler();
        var fileManager = new InMemoryFileManager(standardJavaFileManager(compiler));
        if (!compile(compiler, fileManager)) {
            throw new IllegalStateException("Could not compile");
        }
        return fileManager.compiled();
    }

    private boolean compile(JavaCompiler compiler, InMemoryFileManager fileManager)
        throws IOException {
        return compiler.getTask(
                null,
                fileManager,
                null,
                List.of("--module-path", System.getProperty("jdk.module.path")),
                List.of(),
                List.of(
                    new StringJavaFileObject(
                        Paths.get(normalizedName()).resolve(normalizedName() + ".java"),
                        interpolated(Files.readString(templateDirectory.resolve("class.java")))
                    ),
                    new StringJavaFileObject(
                        Paths.get("module-info.java"),
                        interpolated(
                            Files.readString(templateDirectory.resolve("module-info.java"))
                        )
                    )
                )
            )
            .call();
    }

    private String normalizedName() {
        return values.get("normalizedName");
    }

    private StandardJavaFileManager standardJavaFileManager(JavaCompiler compiler) {
        return compiler.getStandardFileManager(null, Locale.ROOT, StandardCharsets.UTF_8);
    }

    private static final class InMemoryFileManager extends ForwardingJavaFileManager<StandardJavaFileManager> {

        private final Collection<InMemoryJavaFileObject> inMemoryJavaFileObjects =
            new ArrayList<>();

        InMemoryFileManager(StandardJavaFileManager fileManager) {
            super(fileManager);
        }

        @Override
        public JavaFileObject getJavaFileForOutput(
            Location location,
            String className,
            JavaFileObject.Kind kind,
            FileObject sibling
        ) {
            var inMemoryJavaFileObject = new InMemoryJavaFileObject(className);
            inMemoryJavaFileObjects.add(inMemoryJavaFileObject);
            return inMemoryJavaFileObject;
        }

        Collection<FileObject> compiled() {
            return List.copyOf(inMemoryJavaFileObjects);
        }
    }

    private static final class InMemoryJavaFileObject extends SimpleJavaFileObject {

        private final String className;
        private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        InMemoryJavaFileObject(String className) {
            super(URI.create(className.replace('.', '/') + ".class"), Kind.CLASS);
            this.className = className;
        }

        @Override
        public String getName() {
            return className;
        }

        @Override
        public InputStream openInputStream() {
            return new ByteArrayInputStream(outputStream.toByteArray());
        }

        @Override
        public OutputStream openOutputStream() {
            return outputStream;
        }
    }

    private static final class StringJavaFileObject extends SimpleJavaFileObject {

        private final String source;

        StringJavaFileObject(Path path, String source) {
            super(path.toUri(), Kind.SOURCE);
            this.source = source;
        }

        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) {
            return source;
        }
    }
}

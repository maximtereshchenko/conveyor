package com.github.maximtereshchenko.conveyor.domain.test;

import com.github.maximtereshchenko.conveyor.api.port.DependencyDefinition;
import com.github.maximtereshchenko.conveyor.api.port.ProjectDefinition;
import com.github.maximtereshchenko.conveyor.common.api.DependencyScope;
import com.github.maximtereshchenko.conveyor.gson.GsonAdapter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

abstract class GeneratedArtifact {

    private final GsonAdapter gsonAdapter;
    private final String name;
    private final int version;
    private final Collection<GeneratedArtifactDefinition> dependencies;
    private final Collection<GeneratedArtifactDefinition> testDependencies;

    GeneratedArtifact(
        GsonAdapter gsonAdapter,
        String name,
        int version,
        Collection<GeneratedArtifactDefinition> dependencies,
        Collection<GeneratedArtifactDefinition> testDependencies
    ) {
        this.gsonAdapter = gsonAdapter;
        this.name = name;
        this.version = version;
        this.dependencies = List.copyOf(dependencies);
        this.testDependencies = List.copyOf(testDependencies);
    }

    final String name() {
        return name;
    }

    final int version() {
        return version;
    }

    final Collection<GeneratedArtifactDefinition> dependencies() {
        return dependencies;
    }

    final GeneratedArtifactDefinition install(Path directory) throws IOException {
        var fileName = "%s-%d".formatted(name, version);
        var jarPath = directory.resolve(fileName + ".jar");
        writeJar(jarPath, compiled());
        gsonAdapter.write(
            directory.resolve(fileName + ".json"),
            new ProjectDefinition(
                name,
                version,
                null,
                null,
                Map.of(),
                List.of(),
                Stream.concat(
                        dependencies.stream()
                            .map(definition -> dependencyDefinition(definition, DependencyScope.IMPLEMENTATION)),
                        testDependencies.stream()
                            .map(definition -> dependencyDefinition(definition, DependencyScope.TEST))
                    )
                    .toList()
            )
        );
        return new GeneratedArtifactDefinition(
            packageName(),
            packageName() + '.' + className(),
            name,
            version,
            jarPath
        );
    }

    abstract String className();

    abstract String classSourceCode();

    abstract String moduleInfoSourceCode();

    final String packageName() {
        return name.replace("-", "");
    }

    private DependencyDefinition dependencyDefinition(GeneratedArtifactDefinition definition, DependencyScope scope) {
        return new DependencyDefinition(definition.name(), definition.version(), scope);
    }

    private void writeJar(Path path, Collection<FileObject> compiled) throws IOException {
        try (var zipOutputStream = new ZipOutputStream(Files.newOutputStream(path))) {
            for (var fileObject : compiled) {
                zipOutputStream.putNextEntry(new ZipEntry(fileObject.toUri().toString()));
                try (var inputStream = fileObject.openInputStream()) {
                    inputStream.transferTo(zipOutputStream);
                }
                zipOutputStream.closeEntry();
            }
        }
    }

    private StandardJavaFileManager standardJavaFileManager(JavaCompiler compiler) {
        return compiler.getStandardFileManager(null, Locale.ROOT, StandardCharsets.UTF_8);
    }

    private Collection<FileObject> compiled() {
        var compiler = ToolProvider.getSystemJavaCompiler();
        var fileManager = new InMemoryFileManager(standardJavaFileManager(compiler));
        compiler.getTask(
                null,
                fileManager,
                null,
                List.of("--module-path", modulePath()),
                List.of(),
                List.of(
                    new StringJavaFileObject(packageName(), className() + ".java", classSourceCode()),
                    new StringJavaFileObject("", "module-info.java", moduleInfoSourceCode())
                )
            )
            .call();
        return fileManager.compiled();
    }

    private String modulePath() {
        var defaultModulePath = System.getProperty("jdk.module.path");
        if (dependencies.isEmpty()) {
            return defaultModulePath;
        }
        return defaultModulePath +
            dependencies.stream()
                .map(GeneratedArtifactDefinition::jar)
                .map(Path::toString)
                .collect(Collectors.joining(":", ":", ""));
    }

    private static final class InMemoryFileManager extends ForwardingJavaFileManager<StandardJavaFileManager> {

        private final Collection<InMemoryJavaFileObject> inMemoryJavaFileObjects = new ArrayList<>();

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
        public OutputStream openOutputStream() {
            return outputStream;
        }

        @Override
        public InputStream openInputStream() {
            return new ByteArrayInputStream(outputStream.toByteArray());
        }
    }

    private static final class StringJavaFileObject extends SimpleJavaFileObject {

        private final String source;

        StringJavaFileObject(String packageName, String name, String source) {
            super(URI.create(packageName).resolve(name), Kind.SOURCE);
            this.source = source;
        }

        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) {
            return source;
        }
    }
}

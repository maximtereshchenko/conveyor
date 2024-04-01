package com.github.maximtereshchenko.conveyor.domain.test;

import javax.tools.*;
import java.io.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

final class JarBuilder {

    private final String name;
    private final int version;
    private final String classSourceCode;
    private final String moduleInfoSourceCode;

    JarBuilder(String name, int version, String classSourceCode, String moduleInfoSourceCode) {
        this.name = name;
        this.version = version;
        this.classSourceCode = classSourceCode;
        this.moduleInfoSourceCode = moduleInfoSourceCode;
    }

    Path install(Path path) {
        var jar = path.resolve("%s-%d.jar".formatted(name, version));
        writeJar(jar, compiled(path));
        return jar;
    }

    private String modulePath(Path path) {
        try (var entries = Files.list(path)) {
            return Stream.concat(
                    Stream.of(System.getProperty("jdk.module.path")),
                    entries.filter(Files::isRegularFile)
                        .filter(file -> file.getFileName().toString().endsWith(".jar"))
                        .map(Path::toString)
                )
                .collect(Collectors.joining(":"));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void writeJar(Path path, Collection<FileObject> compiled) {
        try (var zipOutputStream = new ZipOutputStream(Files.newOutputStream(path))) {
            for (var fileObject : compiled) {
                zipOutputStream.putNextEntry(new ZipEntry(fileObject.toUri().toString()));
                try (var inputStream = fileObject.openInputStream()) {
                    inputStream.transferTo(zipOutputStream);
                }
                zipOutputStream.closeEntry();
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private Collection<FileObject> compiled(Path path) {
        var compiler = ToolProvider.getSystemJavaCompiler();
        var fileManager = new InMemoryFileManager(standardJavaFileManager(compiler));
        compiler.getTask(
                null,
                fileManager,
                null,
                List.of("--module-path", modulePath(path)),
                List.of(),
                List.of(
                    new StringJavaFileObject(packageName(), className() + ".java", classSourceCode),
                    new StringJavaFileObject("", "module-info.java", moduleInfoSourceCode)
                )
            )
            .call();
        return fileManager.compiled();
    }

    private String packageName() {
        var packageNameWithColon = classSourceCode.split(System.lineSeparator())[0]
            .substring("package ".length());
        return packageNameWithColon.substring(0, packageNameWithColon.length() - 1);
    }

    private String className() {
        return Stream.of(classSourceCode.split(System.lineSeparator()))
            .filter(line -> line.startsWith("public final class "))
            .map(line -> line.substring("public final class ".length()))
            .map(line -> line.split(" ")[0])
            .findAny()
            .orElseThrow();
    }

    private StandardJavaFileManager standardJavaFileManager(JavaCompiler compiler) {
        return compiler.getStandardFileManager(null, Locale.ROOT, StandardCharsets.UTF_8);
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

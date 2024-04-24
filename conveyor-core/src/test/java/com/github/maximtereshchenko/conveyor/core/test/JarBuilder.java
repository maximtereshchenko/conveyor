package com.github.maximtereshchenko.conveyor.core.test;

import com.github.maximtereshchenko.compiler.Compiler;
import com.github.maximtereshchenko.test.common.Directories;
import com.github.maximtereshchenko.zip.ArchiveContainer;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final class JarBuilder {

    private static final Pattern INTERPOLATION_PATTERN = Pattern.compile("\\$\\{([^}]+)}");

    private final Path templateDirectory;
    private final Map<String, String> values = new HashMap<>();
    private final Path temporaryDirectory;
    private final Compiler compiler;

    private JarBuilder(Path templateDirectory, Path temporaryDirectory, Compiler compiler) {
        this.templateDirectory = templateDirectory;
        this.temporaryDirectory = temporaryDirectory;
        this.compiler = compiler;
    }

    static JarBuilder from(String templateDirectory, Path temporaryDirectory, Compiler compiler)
        throws URISyntaxException {
        return new JarBuilder(path(templateDirectory), temporaryDirectory, compiler)
            .group("group")
            .name(templateDirectory)
            .version("1.0.0");
    }

    private static Path path(String templateDirectory) throws URISyntaxException {
        return Paths.get(
            Objects.requireNonNull(
                    Thread.currentThread()
                        .getContextClassLoader()
                        .getResource(templateDirectory)
                )
                .toURI()
        );
    }

    JarBuilder group(String group) {
        values.put("group", group);
        return this;
    }

    JarBuilder name(String name) {
        values.put("name", name);
        values.put("normalizedName", name.replaceAll("[-:.]", ""));
        return this;
    }

    JarBuilder version(String version) {
        values.put("version", String.valueOf(version));
        return this;
    }

    String group() {
        return values.get("group");
    }

    String name() {
        return values.get("name");
    }

    String version() {
        return values.get("version");
    }

    void write(Path path) throws IOException {
        var sources = temporaryDirectory.resolve("sources");
        var classes = temporaryDirectory.resolve("classes");
        compiler.compile(
            Set.of(
                Files.writeString(
                    Directories.createDirectoriesForFile(
                        sources.resolve(normalizedName())
                            .resolve(normalizedName() + ".java")
                    ),
                    interpolated(Files.readString(templateDirectory.resolve("class.java")))
                ),
                Files.writeString(
                    sources.resolve("module-info.java"),
                    interpolated(
                        Files.readString(templateDirectory.resolve("module-info.java"))
                    )
                )
            ),
            Stream.of(System.getProperty("jdk.module.path").split(":"))
                .map(temporaryDirectory.getFileSystem()::getPath)
                .collect(Collectors.toSet()),
            classes
        );
        new ArchiveContainer(classes).archive(path);
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

    private String normalizedName() {
        return values.get("normalizedName");
    }
}

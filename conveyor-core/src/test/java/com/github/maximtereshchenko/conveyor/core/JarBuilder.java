package com.github.maximtereshchenko.conveyor.core;

import com.github.maximtereshchenko.conveyor.compiler.Compiler;
import com.github.maximtereshchenko.conveyor.files.FileTree;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorPlugin;
import com.github.maximtereshchenko.conveyor.zip.ZipArchiveContainer;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final class JarBuilder {

    private static final Pattern INTERPOLATION_PATTERN = Pattern.compile("\\$\\{([^}]+)}");
    private static final Pattern IMPLEMENTS_PATTERN = Pattern.compile(
        "implements ([a-zA-Z<>]+) \\{"
    );

    private final Path template;
    private final Map<String, String> values = new HashMap<>();
    private final Path temporaryDirectory;
    private final Compiler compiler;

    private JarBuilder(Path template, Path temporaryDirectory, Compiler compiler) {
        this.template = template;
        this.temporaryDirectory = temporaryDirectory;
        this.compiler = compiler;
    }

    static JarBuilder from(String template, Path temporaryDirectory, Compiler compiler)
        throws URISyntaxException {
        return new JarBuilder(path(template), temporaryDirectory, compiler)
            .group("group")
            .name(template)
            .version("1.0.0");
    }

    private static Path path(String template) throws URISyntaxException {
        return Paths.get(
            Objects.requireNonNull(
                    Thread.currentThread()
                        .getContextClassLoader()
                        .getResource(template)
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
        var sourceClass = sources.resolve(normalizedName())
            .resolve(normalizedName() + ".java");
        new FileTree(sourceClass).write(interpolated(classJava()));
        compiler.compile(
            Set.of(sourceClass),
            Stream.of(System.getProperty("java.class.path").split(":"))
                .map(Paths::get)
                .collect(Collectors.toSet()),
            classes,
            System.err::println
        );
        new FileTree(
            classes.resolve("META-INF")
                .resolve("services")
                .resolve(service())
        )
            .write(normalizedName() + '.' + normalizedName());
        new ZipArchiveContainer(classes).archive(path);
    }

    private String service() throws IOException {
        var matcher = IMPLEMENTS_PATTERN.matcher(classJava());
        matcher.find();
        return switch (matcher.group(1)) {
            case "ConveyorPlugin" -> ConveyorPlugin.class.getName();
            case "Supplier<String>" -> Supplier.class.getName();
            default -> throw new IllegalArgumentException();
        };
    }

    private String classJava() throws IOException {
        return Files.readString(template);
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

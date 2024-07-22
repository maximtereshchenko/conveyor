package com.github.maximtereshchenko.conveyor.plugin.junit.jupiter;

import com.github.maximtereshchenko.conveyor.junit.jupiter.Launcher;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorTaskAction;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorTaskTracer;
import com.github.maximtereshchenko.conveyor.plugin.api.TracingImportance;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.engine.JupiterTestEngine;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.engine.ConfigurationParameters;
import org.junit.platform.launcher.TestExecutionListener;
import org.opentest4j.AssertionFailedError;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final class RunJunitJupiterTestsAction implements ConveyorTaskAction {

    private final Path classesDirectory;
    private final Path testClassesDirectory;
    private final Set<Path> dependencies;

    RunJunitJupiterTestsAction(
        Path classesDirectory,
        Path testClassesDirectory,
        Set<Path> dependencies
    ) {
        this.classesDirectory = classesDirectory;
        this.testClassesDirectory = testClassesDirectory;
        this.dependencies = dependencies;
    }

    @Override
    public void execute(ConveyorTaskTracer tracer) {
        try {
            var process = process();
            readStandardOut(process.getInputStream(), tracer);
            if (exitCode(process) != 0) {
                throw new IllegalStateException("There are test failures");
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void readStandardOut(InputStream inputStream, ConveyorTaskTracer tracer)
        throws IOException {
        try (var reader = new BufferedReader(new InputStreamReader(inputStream))) {
            var buffer = new StringBuilder();
            for (var line = reader.readLine(); line != null; line = reader.readLine()) {
                if (line.startsWith(">")) {
                    flush(buffer, tracer);
                }
                buffer.append(line).append(System.lineSeparator());
            }
            flush(buffer, tracer);
        }
    }

    private void flush(StringBuilder buffer, ConveyorTaskTracer tracer) {
        if (buffer.isEmpty()) {
            return;
        }
        tracer.submit(TracingImportance.INFO, () -> buffer.substring(1).trim());
        buffer.setLength(0);
    }

    private int exitCode(Process process) {
        try {
            return process.waitFor();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return 1;
        }
    }

    private Process process() throws IOException {
        return new ProcessBuilder(
            "java",
            "--class-path",
            classpath(),
            Launcher.class.getName(),
            testClassesDirectory.toString()
        )
            .redirectErrorStream(true)
            .start();
    }

    private String classpath() {
        return Stream.of(
                dependencies.stream(),
                Stream.of(classesDirectory, testClassesDirectory),
                Stream.of(
                        Launcher.class,
                        TestExecutionListener.class,
                        ConfigurationParameters.class,
                        Preconditions.class,
                        JupiterTestEngine.class,
                        Test.class,
                        AssertionFailedError.class
                    )
                    .map(Class::getProtectionDomain)
                    .map(ProtectionDomain::getCodeSource)
                    .map(CodeSource::getLocation)
                    .map(this::path)
            )
            .flatMap(Function.identity())
            .map(Path::toString)
            .collect(Collectors.joining(":"));
    }

    private Path path(URL url) {
        try {
            return Paths.get(url.toURI());
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }
}

package com.github.maximtereshchenko.conveyor.core;

import com.github.maximtereshchenko.conveyor.api.TracingOutputLevel;
import com.github.maximtereshchenko.conveyor.api.port.TracingOutput;
import com.github.maximtereshchenko.conveyor.api.schematic.SchematicDefinition;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final class Tracer {

    private final TracingOutput output;
    private final TracingOutputLevel outputLevel;
    private final List<String> context;

    private Tracer(TracingOutput output, TracingOutputLevel outputLevel, List<String> context) {
        this.output = output;
        this.outputLevel = outputLevel;
        this.context = context;
    }

    Tracer(TracingOutput output, TracingOutputLevel outputLevel) {
        this(output, outputLevel, List.of());
    }

    Tracer withContext(String name, Object value) {
        var copy = new ArrayList<>(context);
        copy.add(context(name, value));
        return new Tracer(output, outputLevel, copy);
    }

    void submitConstructionOrder(List<Schematic> schematicsInConstructionOrder) {
        submit(Importance.INFO, () -> "Construction order " + schematicsInConstructionOrder);
    }

    void submitDownloadedArtifact(URI uri) {
        submit(Importance.INFO, () -> "Downloaded " + uri);
    }

    void submitConstructionDuration(Instant start, Instant end) {
        submit(
            Importance.INFO,
            () -> "Construction took %ds".formatted(Duration.between(start, end).getSeconds())
        );
    }

    void submitConstruction(Id id, Version version) {
        submit(
            Importance.INFO,
            () -> "%s:%s construction started".formatted(id, version)
        );
    }

    void submitTaskExecution(String task) {
        submit(Importance.INFO, () -> task + " execution started");
    }

    void submitTaskUpToDate(String task) {
        submit(Importance.INFO, () -> task + " inputs and outputs are up to date");
    }

    void submitTaskRestoredFromCache(String task) {
        submit(Importance.INFO, () -> task + " outputs were restored from cache");
    }

    void submit(Importance importance, Supplier<String> supplier, Throwable... throwable) {
        if (!isTraceable(importance)) {
            return;
        }
        output.write(formatted(importance, supplier.get()));
        for (var element : throwable) {
            var writer = new StringWriter();
            element.printStackTrace(new PrintWriter(writer));
            output.write(writer.toString());
        }
    }

    void submitLocalModel(ExtendableLocalInheritanceHierarchyModel model) {
        submitModel("local ", model);
    }

    void submitCompleteModel(CompleteInheritanceHierarchyModel model) {
        submitModel("complete ", model);
    }

    void submitModel(InheritanceHierarchyModel<SchematicModel> model) {
        submitModel("", model);
    }

    void submitSchematicDefinition(SchematicDefinition schematicDefinition, Path path) {
        submit(
            Importance.DEBUG,
            () -> "Schematic definition %s was read from %s".formatted(
                schematicDefinition.group()
                    .map(group -> group + ':' + schematicDefinition.name())
                    .orElse(schematicDefinition.name()),
                path
            )
        );
    }

    void submitClasspath(Set<? extends Artifact> artifacts, Set<Path> classpath) {
        submit(
            Importance.DEBUG,
            () -> "For %s built classpath %s".formatted(
                artifacts.stream()
                    .map(Artifact::id)
                    .toList(),
                classpath
            )
        );
    }

    void submitPlugins(Plugins plugins) {
        submit(Importance.DEBUG, () -> "Loaded plugins " + plugins);
    }

    void submitTasks(Tasks tasks) {
        submit(Importance.DEBUG, () -> "Loaded tasks " + tasks);
    }

    private void submitModel(String modelTag, SchematicModel model) {
        submit(Importance.DEBUG, () -> "Built %smodel %s".formatted(modelTag, model));
    }

    private boolean isTraceable(Importance importance) {
        return importance.minimalOutputLevel().compareTo(outputLevel) <= 0;
    }

    private String formatted(Importance importance, String message) {
        return Stream.concat(
                debugContext(),
                Stream.of(context("importance", importance), context("message", message))
            )
            .collect(Collectors.joining(", "));
    }

    private Stream<String> debugContext() {
        if (!isTraceable(Importance.DEBUG)) {
            return Stream.of();
        }
        return Stream.concat(
            Stream.of(
                context(
                    "timestamp",
                    DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(
                        LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
                    )
                ),
                context("thread", Thread.currentThread().getName())
            ),
            context.stream()
        );
    }

    private String context(String name, Object value) {
        return name + '(' + value + ')';
    }
}

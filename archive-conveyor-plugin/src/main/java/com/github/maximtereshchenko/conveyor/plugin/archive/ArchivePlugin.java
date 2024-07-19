package com.github.maximtereshchenko.conveyor.plugin.archive;

import com.github.maximtereshchenko.conveyor.plugin.api.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class ArchivePlugin implements ConveyorPlugin {

    @Override
    public String name() {
        return "archive-conveyor-plugin";
    }

    @Override
    public List<ConveyorTask> tasks(
        ConveyorSchematic schematic,
        Map<String, String> configuration
    ) {
        var conveyor = schematic.path().getParent().resolve(".conveyor");
        var destination = configuredPath(configuration, "destination")
            .orElseGet(() ->
                conveyor.resolve("%s-%s.jar".formatted(schematic.name(), schematic.version()))
            );
        return List.of(
            new ConveyorTask(
                "archive",
                BindingStage.ARCHIVE,
                BindingStep.RUN,
                new ArchiveAction(
                    configuredPath(configuration, "classes.directory")
                        .orElseGet(() -> conveyor.resolve("classes")),
                    destination
                ),
                Set.of(),
                Set.of(),
                Cache.DISABLED
            ),
            new ConveyorTask(
                "publish-jar-artifact",
                BindingStage.ARCHIVE,
                BindingStep.FINALIZE,
                new PublishJarArtifactTask(destination, schematic),
                Set.of(),
                Set.of(),
                Cache.DISABLED
            )
        );
    }

    private Optional<Path> configuredPath(Map<String, String> configuration, String property) {
        return Optional.ofNullable(configuration.get(property))
            .map(Paths::get)
            .map(Path::toAbsolutePath)
            .map(Path::normalize);
    }
}

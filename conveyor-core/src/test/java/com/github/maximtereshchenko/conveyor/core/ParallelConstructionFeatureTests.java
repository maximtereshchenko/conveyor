package com.github.maximtereshchenko.conveyor.core;

import com.github.maximtereshchenko.conveyor.api.ConveyorModule;
import com.github.maximtereshchenko.conveyor.api.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static com.github.maximtereshchenko.conveyor.common.test.MoreAssertions.assertThat;

final class ParallelConstructionFeatureTests extends ConveyorTest {

    @Test
    void givenExecutor_whenConstructToStage_thenSchematicsAreConstructedInParallel(
        @TempDir Path path,
        ConveyorModuleBuilder moduleBuilder,
        BuilderFactory factory
    ) throws Exception {
        factory.repositoryBuilder(path)
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("instant")
            )
            .jar(
                factory.jarBuilder("instant", path)
            )
            .install(path);
        var project = path.resolve("project");
        var projectDepth1a = project.resolve("project-depth-1a");
        var projectDepth1b = project.resolve("project-depth-1b");
        var projectDepth2a = projectDepth1a.resolve("project-depth-2a");
        var projectDepth2b = projectDepth1b.resolve("project-depth-2b");
        var template = factory.schematicDefinitionBuilder()
            .name("template")
            .repository(path)
            .plugin(
                "group",
                "instant",
                "1.0.0",
                Map.of("instant", "COMPILE-RUN")
            )
            .inclusion(
                factory.schematicDefinitionBuilder()
                    .name("project")
                    .template("template")
                    .inclusion(
                        factory.schematicDefinitionBuilder()
                            .name("project-depth-1a")
                            .template("project")
                            .inclusion(
                                factory.schematicDefinitionBuilder()
                                    .name("project-depth-2a")
                                    .template("project-depth-1a")
                                    .conveyorJson(projectDepth2a)
                            )
                            .conveyorJson(projectDepth1a)
                    )
                    .inclusion(
                        factory.schematicDefinitionBuilder()
                            .name("project-depth-1b")
                            .template("project")
                            .inclusion(
                                factory.schematicDefinitionBuilder()
                                    .name("project-depth-2b")
                                    .template("project-depth-1b")
                                    .conveyorJson(projectDepth2b)
                            )
                            .conveyorJson(projectDepth1b)
                    )
                    .conveyorJson(project)
            )
            .conveyorJson(path);

        assertThat(duration(moduleBuilder.parallel().build(), template, projectDepth2b))
            .isLessThan(duration(moduleBuilder.build(), template, projectDepth2b));
    }

    private Duration duration(ConveyorModule module, Path schematic, Path instantDirectory)
        throws IOException {
        var now = Instant.now();
        module.construct(schematic, List.of(Stage.COMPILE));
        return Duration.between(now, instant(instantDirectory.resolve("instant")));
    }
}

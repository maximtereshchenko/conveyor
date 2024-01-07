package com.github.maximtereshchenko.conveyor.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.maximtereshchenko.conveyor.api.BuildSucceeded;
import com.github.maximtereshchenko.conveyor.api.ConveyorModule;
import com.github.maximtereshchenko.conveyor.api.CouldNotFindProjectDefinition;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

final class BuildProjectUseCaseTests {

    private final ConveyorModule module = new ConveyorFacade();

    @Test
    void givenNoProjectDefinition_whenBuild_thenCouldNotFindProjectDefinitionReturned(@TempDir Path path) {
        var nonExistentFile = path.resolve("conveyor.json");

        assertThat(module.build(nonExistentFile)).isEqualTo(new CouldNotFindProjectDefinition(nonExistentFile));
    }

    @Test
    void givenProjectDefinition_whenBuild_thenProjectIsBuilt() throws Exception {
        var conveyorJson = Paths.get(
            Objects.requireNonNull(
                    Thread.currentThread()
                        .getContextClassLoader()
                        .getResource("conveyor.json")
                )
                .toURI()
        );

        assertThat(module.build(conveyorJson))
            .isEqualTo(new BuildSucceeded(conveyorJson, "test-project", 1));
    }
}

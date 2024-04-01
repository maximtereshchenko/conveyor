package com.github.maximtereshchenko.conveyor.domain.test;

import com.github.maximtereshchenko.conveyor.api.ConveyorModule;
import com.github.maximtereshchenko.conveyor.api.exception.CouldNotFindProjectDefinition;
import com.github.maximtereshchenko.conveyor.common.api.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

final class ExceptionTests extends ConveyorTest {

    @Test
    void givenNoProjectDefinition_whenBuild_thenCouldNotFindProjectDefinitionReturned(
        @TempDir Path path,
        ConveyorModule module
    ) {
        var nonExistent = path.resolve("non-existent.json");

        assertThatThrownBy(() -> module.build(nonExistent, Stage.COMPILE))
            .isInstanceOf(CouldNotFindProjectDefinition.class)
            .hasMessage(nonExistent.toString());
    }
}

package com.github.maximtereshchenko.conveyor.plugin.resources.test;

import com.github.maximtereshchenko.conveyor.common.api.ProductType;
import com.github.maximtereshchenko.conveyor.common.api.Stage;
import com.github.maximtereshchenko.conveyor.common.api.Step;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorTaskBinding;
import com.github.maximtereshchenko.conveyor.plugin.test.ConveyorTaskBindings;
import com.github.maximtereshchenko.conveyor.plugin.test.FakeConveyorSchematicBuilder;
import com.github.maximtereshchenko.test.common.Directories;
import com.github.maximtereshchenko.test.common.JimfsExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@ExtendWith(JimfsExtension.class)
final class CopyResourcesTests {

    public static Stream<Arguments> resources() {
        return Directories.differentDirectoryEntries()
            .flatMap(resources ->
                Stream.of(
                    arguments("main", resources, ProductType.EXPLODED_MODULE),
                    arguments("test", resources, ProductType.EXPLODED_TEST_MODULE)
                )
            );
    }

    @Test
    void givenPlugin_whenBindings_thenCopyResourcesBindingReturned(Path path) {
        ConveyorTaskBindings.from(FakeConveyorSchematicBuilder.discoveryDirectory(path).build())
            .assertThat()
            .extracting(ConveyorTaskBinding::stage, ConveyorTaskBinding::step)
            .contains(tuple(Stage.COMPILE, Step.PREPARE), tuple(Stage.TEST, Step.PREPARE));
    }

    @ParameterizedTest
    @EnumSource(value = ProductType.class, names = {"EXPLODED_MODULE", "EXPLODED_TEST_MODULE"})
    void givenNoResources_whenExecuteTasks_thenExplodedModuleIsNotModified(
        ProductType productType,
        Path path
    ) throws IOException {
        var explodedModule = Files.createDirectory(path.resolve("exploded-module"));
        var schematic = FakeConveyorSchematicBuilder.discoveryDirectory(path).build();

        ConveyorTaskBindings.from(schematic)
            .executeTasks(schematic.product(explodedModule, productType));

        assertThat(explodedModule).isEmptyDirectory();
    }

    @ParameterizedTest
    @MethodSource("resources")
    void givenResources_whenExecuteTasks_thenExplodedModuleContainsResources(
        String sourceSet,
        Set<String> resources,
        ProductType productType,
        Path path
    ) throws IOException {
        var explodedModule = path.resolve("exploded-module");
        var resourcesDirectory = Directories.writeFiles(
            resourcesDirectory(path, sourceSet),
            resources
        );
        var schematic = FakeConveyorSchematicBuilder.discoveryDirectory(path).build();

        ConveyorTaskBindings.from(schematic)
            .executeTasks(schematic.product(explodedModule, productType));

        Directories.assertThatDirectoryContentsEqual(explodedModule, resourcesDirectory);
    }

    private Path resourcesDirectory(Path path, String sourceSet) {
        return path.resolve("src").resolve(sourceSet).resolve("resources");
    }
}

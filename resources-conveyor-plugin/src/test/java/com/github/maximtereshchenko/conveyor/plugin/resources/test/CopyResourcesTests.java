package com.github.maximtereshchenko.conveyor.plugin.resources.test;

import com.github.maximtereshchenko.conveyor.common.api.ProductType;
import com.github.maximtereshchenko.conveyor.common.api.Stage;
import com.github.maximtereshchenko.conveyor.common.api.Step;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorTaskBinding;
import com.github.maximtereshchenko.conveyor.plugin.test.ConveyorTaskBindings;
import com.github.maximtereshchenko.conveyor.plugin.test.FakeConveyorSchematicBuilder;
import com.github.maximtereshchenko.jimfs.JimfsExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

@ExtendWith(JimfsExtension.class)
final class CopyResourcesTests {

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
    @CsvSource(
        textBlock = """
                    main, EXPLODED_MODULE
                    test, EXPLODED_TEST_MODULE
                    """
    )
    void givenResource_whenExecuteTasks_thenExplodedModuleContainsResource(
        String sourceSet,
        ProductType productType,
        Path path
    ) throws IOException {
        var explodedModule = Files.createDirectory(path.resolve("exploded-module"));
        var resource = resourcesDirectory(path, sourceSet).resolve("resource");
        writeResource(resource);
        var schematic = FakeConveyorSchematicBuilder.discoveryDirectory(path).build();

        ConveyorTaskBindings.from(schematic)
            .executeTasks(schematic.product(explodedModule, productType));

        assertThat(explodedModule.resolve("resource"))
            .exists()
            .hasSameTextualContentAs(resource);
    }

    @ParameterizedTest
    @CsvSource(
        textBlock = """
                    main, EXPLODED_MODULE
                    test, EXPLODED_TEST_MODULE
                    """
    )
    void givenMultipleResources_whenExecuteTasks_thenExplodedModuleContainsResources(
        String sourceSet,
        ProductType productType,
        Path path
    ) throws IOException {
        var explodedModule = Files.createDirectory(path.resolve("exploded-module"));
        var firstResource = resourcesDirectory(path, sourceSet).resolve("firstResource");
        var secondResource = resourcesDirectory(path, sourceSet).resolve("secondResource");
        writeResource(firstResource);
        writeResource(secondResource);
        var schematic = FakeConveyorSchematicBuilder.discoveryDirectory(path).build();

        ConveyorTaskBindings.from(schematic)
            .executeTasks(schematic.product(explodedModule, productType));

        assertThat(explodedModule.resolve("firstResource"))
            .exists()
            .hasSameTextualContentAs(firstResource);
        assertThat(explodedModule.resolve("secondResource"))
            .exists()
            .hasSameTextualContentAs(secondResource);
    }

    @ParameterizedTest
    @CsvSource(
        textBlock = """
                    main, EXPLODED_MODULE
                    test, EXPLODED_TEST_MODULE
                    """
    )
    void givenNestedResources_whenExecuteTasks_thenExplodedModuleContainsResources(
        String sourceSet,
        ProductType productType,
        Path path
    ) throws IOException {
        var explodedModule = Files.createDirectory(path.resolve("exploded-module"));
        var resource = resourcesDirectory(path, sourceSet).resolve("directory").resolve("resource");
        writeResource(resource);
        var schematic = FakeConveyorSchematicBuilder.discoveryDirectory(path).build();

        ConveyorTaskBindings.from(schematic)
            .executeTasks(schematic.product(explodedModule, productType));

        assertThat(explodedModule.resolve("directory").resolve("resource"))
            .exists()
            .hasSameTextualContentAs(resource);
    }

    void writeResource(Path path) throws IOException {
        Files.createDirectories(path.getParent());
        Files.writeString(path, path.getFileName().toString());
    }

    private Path resourcesDirectory(Path path, String sourceSet) {
        return path.resolve("src").resolve(sourceSet).resolve("resources");
    }
}

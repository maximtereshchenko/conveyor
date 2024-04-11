package com.github.maximtereshchenko.conveyor.plugin.resources.test;

import com.github.maximtereshchenko.conveyor.common.api.ProductType;
import com.github.maximtereshchenko.conveyor.common.api.Stage;
import com.github.maximtereshchenko.conveyor.common.api.Step;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorTaskBinding;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

import java.io.IOException;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

final class CopyResourcesTests extends ResourcesPluginTest {

    @Test
    void givenPlugin_whenBindings_thenCopyResourcesBindingReturned(Path path) {
        assertThat(bindings(path))
            .extracting(ConveyorTaskBinding::stage, ConveyorTaskBinding::step)
            .contains(tuple(Stage.COMPILE, Step.PREPARE), tuple(Stage.TEST, Step.PREPARE));
    }

    @ParameterizedTest
    @EnumSource(value = ProductType.class, names = {"EXPLODED_MODULE", "EXPLODED_TEST_MODULE"})
    void givenNoResources_whenExecuteTasks_thenExplodedModuleIsNotModified(
        ProductType productType,
        Path path
    ) throws IOException {
        var explodedModule = path.resolve("exploded-module");

        executeTasks(path, explodedModule, productType);

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
        var explodedModule = path.resolve("exploded-module");
        var resource = resourcesDirectory(path, sourceSet).resolve("resource");
        writeResource(resource);

        executeTasks(path, explodedModule, productType);

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
        var explodedModule = path.resolve("exploded-module");
        var firstResource = resourcesDirectory(path, sourceSet).resolve("firstResource");
        var secondResource = resourcesDirectory(path, sourceSet).resolve("secondResource");
        writeResource(firstResource);
        writeResource(secondResource);

        executeTasks(path, explodedModule, productType);

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
        var explodedModule = path.resolve("exploded-module");
        var resource = resourcesDirectory(path, sourceSet).resolve("directory").resolve("resource");
        writeResource(resource);

        executeTasks(path, explodedModule, productType);

        assertThat(explodedModule.resolve("directory").resolve("resource"))
            .exists()
            .hasSameTextualContentAs(resource);
    }

    private Path resourcesDirectory(Path path, String sourceSet) {
        return path.resolve("src").resolve(sourceSet).resolve("resources");
    }
}

package com.github.maximtereshchenko.conveyor.domain.test;

import com.github.maximtereshchenko.conveyor.api.ConveyorModule;
import com.github.maximtereshchenko.conveyor.common.api.DependencyScope;
import com.github.maximtereshchenko.conveyor.common.api.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

final class DependencyVersionResolutionFeatureTests extends ConveyorTest {

    @Test
    void givenTestDependencyRequireHigherVersion_whenConstructToStage_thenDependencyIsUsedWithLowerVersion(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) {
        factory.repositoryBuilder()
            .superManual()
            .manual(builder ->
                builder.name("module-path")
                    .version(1)
                    .dependency("dependency", 1, DependencyScope.IMPLEMENTATION)
                    .dependency("test", 1, DependencyScope.TEST)
            )
            .jar("module-path", builder -> builder.name("module-path").version(1))
            .manual(builder -> builder.name("dependency").version(1))
            .jar("dependency", builder -> builder.name("dependency").version(1))
            .manual(builder ->
                builder.name("test")
                    .version(1)
                    .dependency("dependency", 2, DependencyScope.IMPLEMENTATION)
            )
            .manual(builder -> builder.name("dependency").version(2))
            .install(path);

        module.construct(
            factory.schematicBuilder()
                .name("project")
                .version(1)
                .repository("main", path, true)
                .plugin("module-path", 1, Map.of())
                .install(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(path).resolve("module-path"))
            .content(StandardCharsets.UTF_8)
            .isEqualTo("dependency-1");
    }

    @Test
    void givenPluginsRequireCommonDependency_whenConstructToStage_thenDependencyIsUsedWithHighestVersion(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) {
        factory.repositoryBuilder()
            .superManual()
            .manual(builder ->
                builder.name("first")
                    .version(1)
                    .dependency("dependency", 1, DependencyScope.IMPLEMENTATION)
            )
            .jar("module-path", builder -> builder.name("first").version(1))
            .manual(builder ->
                builder.name("second")
                    .version(1)
                    .dependency("dependency", 2, DependencyScope.IMPLEMENTATION)
            )
            .jar("module-path", builder -> builder.name("second").version(1))
            .manual(builder -> builder.name("dependency").version(1))
            .manual(builder -> builder.name("dependency").version(2))
            .jar("dependency", builder -> builder.name("dependency").version(2))
            .install(path);

        module.construct(
            factory.schematicBuilder()
                .name("project")
                .version(1)
                .repository("main", path, true)
                .plugin("first", 1, Map.of())
                .plugin("second", 1, Map.of())
                .install(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(path).resolve("module-path"))
            .content(StandardCharsets.UTF_8)
            .isEqualTo("dependency-2");
    }

    @Test
    void givenHighestDependencyVersionRequiredByExcludedDependency_whenConstructToStage_thenPluginUsesLowerVersionDependency(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) {
        factory.repositoryBuilder()
            .superManual()
            .manual(builder ->
                builder.name("first")
                    .version(1)
                    .dependency("should-not-be-affected", 1, DependencyScope.IMPLEMENTATION)
                    .dependency("exclude-affecting", 1, DependencyScope.IMPLEMENTATION)
            )
            .jar("module-path", builder -> builder.name("first").version(1))
            .manual(builder ->
                builder.name("second")
                    .version(1)
                    .dependency("will-affect", 1, DependencyScope.IMPLEMENTATION)
            )
            .jar("module-path", builder -> builder.name("second").version(1))
            .manual(builder -> builder.name("should-not-be-affected").version(1))
            .jar("dependency", builder -> builder.name("should-not-be-affected").version(1))
            .manual(builder ->
                builder.name("exclude-affecting")
                    .version(1)
                    .dependency("will-affect", 2, DependencyScope.IMPLEMENTATION)
            )
            .jar("dependency", builder -> builder.name("exclude-affecting").version(1))
            .manual(builder ->
                builder.name("will-affect")
                    .version(1)
                    .dependency("should-not-be-affected", 2, DependencyScope.IMPLEMENTATION)
            )
            .manual(builder -> builder.name("should-not-be-affected").version(2))
            .manual(builder -> builder.name("will-affect").version(2))
            .jar("dependency", builder -> builder.name("will-affect").version(2))
            .install(path);

        module.construct(
            factory.schematicBuilder()
                .name("project")
                .version(1)
                .repository("main", path, true)
                .plugin("first", 1, Map.of())
                .plugin("second", 1, Map.of())
                .install(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(path).resolve("module-path"))
            .content(StandardCharsets.UTF_8)
            .hasLineCount(3)
            .contains("should-not-be-affected-1", "exclude-affecting-1", "will-affect-2");
    }

    @Test
    void givenTestDependencyRequireOtherDependencyHigherVersion_whenConstructToStage_thenOtherDependencyIsUsedWithLowerVersion(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) {
        factory.repositoryBuilder()
            .superManual()
            .manual(builder -> builder.name("dependencies").version(1))
            .jar("dependencies", builder -> builder.name("dependencies").version(1))
            .manual(builder -> builder.name("dependency").version(1))
            .jar("dependency", builder -> builder.name("dependency").version(1))
            .manual(builder ->
                builder.name("test")
                    .version(1)
                    .dependency("dependency", 2, DependencyScope.IMPLEMENTATION)
            )
            .manual(builder -> builder.name("dependency").version(2))
            .install(path);

        module.construct(
            factory.schematicBuilder()
                .name("project")
                .version(1)
                .repository("main", path, true)
                .plugin("dependencies", 1, Map.of())
                .dependency("dependency", 1, DependencyScope.IMPLEMENTATION)
                .dependency("test", 1, DependencyScope.TEST)
                .install(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(path).resolve("dependencies"))
            .content()
            .isEqualTo("dependency-1");
    }

    @Test
    void givenSchematicRequiresCommonDependency_whenConstructToStage_thenDependencyIsUsedWithHighestVersion(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) {
        factory.repositoryBuilder()
            .superManual()
            .manual(builder -> builder.name("dependencies").version(1))
            .jar("dependencies", builder -> builder.name("dependencies").version(1))
            .manual(builder ->
                builder.name("first")
                    .version(1)
                    .dependency("dependency", 1, DependencyScope.IMPLEMENTATION)
            )
            .jar("dependency", builder -> builder.name("first").version(1))
            .manual(builder ->
                builder.name("second")
                    .version(1)
                    .dependency("dependency", 2, DependencyScope.IMPLEMENTATION)
            )
            .jar("dependency", builder -> builder.name("second").version(1))
            .manual(builder -> builder.name("dependency").version(1))
            .manual(builder -> builder.name("dependency").version(2))
            .jar("dependency", builder -> builder.name("dependency").version(2))
            .install(path);

        module.construct(
            factory.schematicBuilder()
                .name("project")
                .version(1)
                .repository("main", path, true)
                .plugin("dependencies", 1, Map.of())
                .dependency("first", 1, DependencyScope.IMPLEMENTATION)
                .dependency("second", 1, DependencyScope.IMPLEMENTATION)
                .install(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(path).resolve("dependencies"))
            .content(StandardCharsets.UTF_8)
            .hasLineCount(3)
            .contains("first-1", "second-1", "dependency-2");
    }

    @Test
    void givenHighestDependencyVersionRequiredByExcludedDependency_whenConstructToStage_thenDependencyIsUsedWithLowerVersion(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) {
        factory.repositoryBuilder()
            .superManual()
            .manual(builder -> builder.name("dependencies").version(1))
            .jar("dependencies", builder -> builder.name("dependencies").version(1))
            .manual(builder ->
                builder.name("first")
                    .version(1)
                    .dependency("should-not-be-affected", 1, DependencyScope.IMPLEMENTATION)
                    .dependency("exclude-affecting", 1, DependencyScope.IMPLEMENTATION)
            )
            .jar("dependency", builder -> builder.name("first").version(1))
            .manual(builder ->
                builder.name("second")
                    .version(1)
                    .dependency("will-affect", 1, DependencyScope.IMPLEMENTATION)
            )
            .jar("dependency", builder -> builder.name("second").version(1))
            .manual(builder -> builder.name("should-not-be-affected").version(1))
            .jar("dependency", builder -> builder.name("should-not-be-affected").version(1))
            .manual(builder ->
                builder.name("exclude-affecting")
                    .version(1)
                    .dependency("will-affect", 2, DependencyScope.IMPLEMENTATION)
            )
            .jar("dependency", builder -> builder.name("exclude-affecting").version(1))
            .manual(builder ->
                builder.name("will-affect")
                    .version(1)
                    .dependency("should-not-be-affected", 2, DependencyScope.IMPLEMENTATION)
            )
            .manual(builder -> builder.name("should-not-be-affected").version(2))
            .manual(builder -> builder.name("will-affect").version(2))
            .jar("dependency", builder -> builder.name("will-affect").version(2))
            .install(path);

        module.construct(
            factory.schematicBuilder()
                .name("project")
                .version(1)
                .repository("main", path, true)
                .plugin("dependencies", 1, Map.of())
                .dependency("first", 1, DependencyScope.IMPLEMENTATION)
                .dependency("second", 1, DependencyScope.IMPLEMENTATION)
                .install(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(path).resolve("dependencies"))
            .content()
            .hasLineCount(5)
            .contains("first-1", "second-1", "should-not-be-affected-1", "exclude-affecting-1", "will-affect-2");
    }
}

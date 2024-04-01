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
import static org.assertj.core.api.Assertions.assertThatCode;

final class ModulePathTests extends ConveyorTest {

    @Test
    void givenPluginsRequireCommonDependency_whenBuild_thenDependencyUsedWithHigherVersion(
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
            .jar("module-path-conveyor-plugin", builder -> builder.name("first").version(1))
            .manual(builder ->
                builder.name("second")
                    .version(1)
                    .dependency("dependency", 2, DependencyScope.IMPLEMENTATION)
            )
            .jar("module-path-conveyor-plugin", builder -> builder.name("second").version(1))
            .manual(builder -> builder.name("dependency").version(1))
            .manual(builder -> builder.name("dependency").version(2))
            .jar("dependency", builder -> builder.name("dependency").version(2))
            .install(path);

        module.construct(
            factory.schematicBuilder()
                .repository(path)
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
    void givenPluginRequireTransitiveDependency_whenBuild_thenTransitiveDependencyLoaded(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) {
        factory.repositoryBuilder()
            .superManual()
            .manual(builder ->
                builder.name("plugin")
                    .version(1)
                    .dependency("dependency", 1, DependencyScope.IMPLEMENTATION)
            )
            .jar("module-path-conveyor-plugin", builder -> builder.name("plugin").version(1))
            .manual(builder ->
                builder.name("dependency")
                    .version(1)
                    .dependency("transitive", 1, DependencyScope.IMPLEMENTATION)
            )
            .jar("dependency", builder -> builder.name("dependency").version(1))
            .manual(builder -> builder.name("transitive").version(1))
            .jar("dependency", builder -> builder.name("transitive").version(1))
            .install(path);

        module.construct(
            factory.schematicBuilder()
                .repository(path)
                .plugin("plugin", 1, Map.of())
                .install(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(path).resolve("module-path"))
            .content(StandardCharsets.UTF_8)
            .contains("dependency-1", "transitive-1");
    }

    @Test
    void givenPluginRequireDependencyWithTestScope_whenBuild_thenDependencyIsNotLoaded(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) {
        factory.repositoryBuilder()
            .superManual()
            .manual(builder ->
                builder.name("plugin")
                    .version(1)
                    .dependency("test", 1, DependencyScope.TEST)
            )
            .jar("module-path-conveyor-plugin", builder -> builder.name("plugin").version(1))
            .install(path);

        var conveyorJson = factory.schematicBuilder()
            .repository(path)
            .plugin("plugin", 1, Map.of())
            .install(path);

        assertThatCode(() -> module.construct(conveyorJson, Stage.COMPILE)).doesNotThrowAnyException();
    }

    @Test
    void givenPluginRequireTransitiveDependency_whenBuildWithHigherVersion_thenTransitiveDependencyExcluded(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) {
        factory.repositoryBuilder()
            .superManual()
            .manual(builder ->
                builder.name("first")
                    .version(1)
                    .dependency("common", 1, DependencyScope.IMPLEMENTATION)
            )
            .jar("module-path-conveyor-plugin", builder -> builder.name("first").version(1))
            .manual(builder ->
                builder.name("second")
                    .version(1)
                    .dependency("dependency", 1, DependencyScope.IMPLEMENTATION)
            )
            .jar("module-path-conveyor-plugin", builder -> builder.name("second").version(1))
            .manual(builder ->
                builder.name("common")
                    .version(1)
                    .dependency("transitive", 1, DependencyScope.IMPLEMENTATION)
            )
            .manual(builder ->
                builder.name("dependency")
                    .version(1)
                    .dependency("common", 2, DependencyScope.IMPLEMENTATION)
            )
            .jar("dependency", builder -> builder.name("dependency").version(1))
            .manual(builder -> builder.name("transitive").version(1))
            .manual(builder -> builder.name("common").version(2))
            .jar("dependency", builder -> builder.name("common").version(2))
            .install(path);

        module.construct(
            factory.schematicBuilder()
                .repository(path)
                .plugin("first", 1, Map.of())
                .plugin("second", 1, Map.of())
                .install(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(path).resolve("module-path"))
            .content(StandardCharsets.UTF_8)
            .contains("dependency-1", "common-2");
    }

    @Test
    void givenDependencyAffectResolvedVersions_whenBuildWithDependencyExcluded_thenItShouldNotAffectVersions(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) {
        factory.repositoryBuilder()
            .superManual()
            .manual(builder ->
                builder.name("plugin")
                    .version(1)
                    .dependency("should-not-be-updated", 1, DependencyScope.IMPLEMENTATION)
                    .dependency("can-affect-versions", 1, DependencyScope.IMPLEMENTATION)
                    .dependency("will-remove-dependency", 1, DependencyScope.IMPLEMENTATION)
            )
            .jar("module-path-conveyor-plugin", builder -> builder.name("plugin").version(1))
            .manual(builder -> builder.name("should-not-be-updated").version(1))
            .jar("dependency", builder -> builder.name("should-not-be-updated").version(1))
            .manual(builder ->
                builder.name("can-affect-versions")
                    .version(1)
                    .dependency("should-not-be-updated", 2, DependencyScope.IMPLEMENTATION)
            )
            .manual(builder ->
                builder.name("will-remove-dependency")
                    .version(1)
                    .dependency("can-affect-versions", 2, DependencyScope.IMPLEMENTATION)
            )
            .jar("dependency", builder -> builder.name("will-remove-dependency").version(1))
            .manual(builder -> builder.name("should-not-be-updated").version(2))
            .manual(builder -> builder.name("can-affect-versions").version(2))
            .jar("dependency", builder -> builder.name("can-affect-versions").version(2))
            .install(path);

        module.construct(
            factory.schematicBuilder()
                .repository(path)
                .plugin("plugin", 1, Map.of())
                .install(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(path).resolve("module-path"))
            .content(StandardCharsets.UTF_8)
            .contains("will-remove-dependency-1", "can-affect-versions-2", "should-not-be-updated-1")
            .doesNotContain("should-not-be-updated-2");
    }

    @Test
    void givenDependenciesWithDifferentScope_whenBuild_thenDependenciesFilteredByScope(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) {
        factory.repositoryBuilder()
            .superManual()
            .manual(builder -> builder.name("plugin").version(1))
            .jar("dependencies-conveyor-plugin", builder -> builder.name("plugin").version(1))
            .manual(builder -> builder.name("implementation").version(1))
            .emptyJar("implementation", 1)
            .manual(builder -> builder.name("test").version(1))
            .emptyJar("test", 1)
            .install(path);

        module.construct(
            factory.schematicBuilder()
                .repository(path)
                .plugin("plugin", 1, Map.of())
                .dependency("implementation", 1, DependencyScope.IMPLEMENTATION)
                .dependency("test", 1, DependencyScope.TEST)
                .install(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(path).resolve("dependencies-implementation"))
            .content(StandardCharsets.UTF_8)
            .contains(path.resolve("implementation-1.jar").toString());
        assertThat(defaultConstructionDirectory(path).resolve("dependencies-test"))
            .content(StandardCharsets.UTF_8)
            .contains(path.resolve("test-1.jar").toString());
    }

    @Test
    void givenTransitiveDependencyWithTestScope_whenBuild_thenDependencyIsNotLoaded(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) {
        factory.repositoryBuilder()
            .superManual()
            .manual(builder -> builder.name("plugin").version(1))
            .jar("dependencies-conveyor-plugin", builder -> builder.name("plugin").version(1))
            .manual(builder ->
                builder.name("dependency")
                    .version(1)
                    .dependency("test", 1, DependencyScope.TEST)
            )
            .emptyJar("dependency", 1)
            .manual(builder -> builder.name("test").version(1))
            .install(path);

        module.construct(
            factory.schematicBuilder()
                .repository(path)
                .plugin("plugin", 1, Map.of())
                .dependency("dependency", 1, DependencyScope.IMPLEMENTATION)
                .install(path),
            Stage.COMPILE
        );

        var testJarPath = path.resolve("test-1.jar").toString();
        assertThat(defaultConstructionDirectory(path).resolve("dependencies-implementation"))
            .content(StandardCharsets.UTF_8)
            .doesNotContain(testJarPath);
        assertThat(defaultConstructionDirectory(path).resolve("dependencies-test"))
            .content(StandardCharsets.UTF_8)
            .doesNotContain(testJarPath);
    }

    @Test
    void givenDependencyWithTestScope_whenBuild_thenDependencyDidNotAffectImplementationScope(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) {
        factory.repositoryBuilder()
            .superManual()
            .manual(builder -> builder.name("plugin").version(1))
            .jar("dependencies-conveyor-plugin", builder -> builder.name("plugin").version(1))
            .manual(builder -> builder.name("should-not-be-updated").version(1))
            .emptyJar("should-not-be-updated", 1)
            .manual(builder ->
                builder.name("test")
                    .version(1)
                    .dependency("should-not-be-updated", 2, DependencyScope.IMPLEMENTATION)
            )
            .emptyJar("test", 1)
            .manual(builder -> builder.name("should-not-be-updated").version(2))
            .emptyJar("should-not-be-updated", 2)
            .install(path);

        module.construct(
            factory.schematicBuilder()
                .repository(path)
                .plugin("plugin", 1, Map.of())
                .dependency("should-not-be-updated", 1, DependencyScope.IMPLEMENTATION)
                .dependency("test", 1, DependencyScope.TEST)
                .install(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(path).resolve("dependencies-implementation"))
            .content(StandardCharsets.UTF_8)
            .isEqualTo(path.resolve("should-not-be-updated-1.jar").toString());
    }
}

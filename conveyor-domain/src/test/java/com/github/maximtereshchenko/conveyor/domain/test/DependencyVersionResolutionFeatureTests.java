package com.github.maximtereshchenko.conveyor.domain.test;

import com.github.maximtereshchenko.conveyor.api.ConveyorModule;
import com.github.maximtereshchenko.conveyor.common.api.DependencyScope;
import com.github.maximtereshchenko.conveyor.common.api.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

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
                    .version("1.0.0")
                    .dependency("dependency", "1.0.0", DependencyScope.IMPLEMENTATION)
                    .dependency("test", "1.0.0", DependencyScope.TEST)
            )
            .jar("module-path", builder -> builder.name("module-path").version("1.0.0"))
            .manual(builder -> builder.name("dependency").version("1.0.0"))
            .jar("dependency", builder -> builder.name("dependency").version("1.0.0"))
            .manual(builder ->
                builder.name("test")
                    .version("1.0.0")
                    .dependency("dependency", "2.0.0", DependencyScope.IMPLEMENTATION)
            )
            .manual(builder -> builder.name("dependency").version("2.0.0"))
            .install(path);

        module.construct(
            factory.schematicBuilder()
                .name("project")
                .version("1.0.0")
                .repository("main", path, true)
                .plugin("module-path", "1.0.0", Map.of())
                .install(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(path).resolve("module-path"))
            .content(StandardCharsets.UTF_8)
            .isEqualTo("dependency-1.0.0");
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
                    .version("1.0.0")
                    .dependency("dependency", "1.0.0", DependencyScope.IMPLEMENTATION)
            )
            .jar("module-path", builder -> builder.name("first").version("1.0.0"))
            .manual(builder ->
                builder.name("second")
                    .version("1.0.0")
                    .dependency("dependency", "2.0.0", DependencyScope.IMPLEMENTATION)
            )
            .jar("module-path", builder -> builder.name("second").version("1.0.0"))
            .manual(builder -> builder.name("dependency").version("1.0.0"))
            .manual(builder -> builder.name("dependency").version("2.0.0"))
            .jar("dependency", builder -> builder.name("dependency").version("2.0.0"))
            .install(path);

        module.construct(
            factory.schematicBuilder()
                .name("project")
                .version("1.0.0")
                .repository("main", path, true)
                .plugin("first", "1.0.0", Map.of())
                .plugin("second", "1.0.0", Map.of())
                .install(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(path).resolve("module-path"))
            .content(StandardCharsets.UTF_8)
            .isEqualTo("dependency-2.0.0");
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
                    .version("1.0.0")
                    .dependency("should-not-be-affected", "1.0.0", DependencyScope.IMPLEMENTATION)
                    .dependency("exclude-affecting", "1.0.0", DependencyScope.IMPLEMENTATION)
            )
            .jar("module-path", builder -> builder.name("first").version("1.0.0"))
            .manual(builder ->
                builder.name("second")
                    .version("1.0.0")
                    .dependency("will-affect", "1.0.0", DependencyScope.IMPLEMENTATION)
            )
            .jar("module-path", builder -> builder.name("second").version("1.0.0"))
            .manual(builder -> builder.name("should-not-be-affected").version("1.0.0"))
            .jar("dependency", builder -> builder.name("should-not-be-affected").version("1.0.0"))
            .manual(builder ->
                builder.name("exclude-affecting")
                    .version("1.0.0")
                    .dependency("will-affect", "2.0.0", DependencyScope.IMPLEMENTATION)
            )
            .jar("dependency", builder -> builder.name("exclude-affecting").version("1.0.0"))
            .manual(builder ->
                builder.name("will-affect")
                    .version("1.0.0")
                    .dependency("should-not-be-affected", "2.0.0", DependencyScope.IMPLEMENTATION)
            )
            .manual(builder -> builder.name("should-not-be-affected").version("2.0.0"))
            .manual(builder -> builder.name("will-affect").version("2.0.0"))
            .jar("dependency", builder -> builder.name("will-affect").version("2.0.0"))
            .install(path);

        module.construct(
            factory.schematicBuilder()
                .name("project")
                .version("1.0.0")
                .repository("main", path, true)
                .plugin("first", "1.0.0", Map.of())
                .plugin("second", "1.0.0", Map.of())
                .install(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(path).resolve("module-path"))
            .content(StandardCharsets.UTF_8)
            .hasLineCount(3)
            .contains("should-not-be-affected-1.0.0", "exclude-affecting-1.0.0", "will-affect-2.0.0");
    }

    @Test
    void givenTestDependencyRequireOtherDependencyHigherVersion_whenConstructToStage_thenOtherDependencyIsUsedWithLowerVersion(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) {
        factory.repositoryBuilder()
            .superManual()
            .manual(builder -> builder.name("dependencies").version("1.0.0"))
            .jar("dependencies", builder -> builder.name("dependencies").version("1.0.0"))
            .manual(builder -> builder.name("dependency").version("1.0.0"))
            .jar("dependency", builder -> builder.name("dependency").version("1.0.0"))
            .manual(builder ->
                builder.name("test")
                    .version("1.0.0")
                    .dependency("dependency", "2.0.0", DependencyScope.IMPLEMENTATION)
            )
            .manual(builder -> builder.name("dependency").version("2.0.0"))
            .install(path);

        module.construct(
            factory.schematicBuilder()
                .name("project")
                .version("1.0.0")
                .repository("main", path, true)
                .plugin("dependencies", "1.0.0", Map.of())
                .dependency("dependency", "1.0.0", DependencyScope.IMPLEMENTATION)
                .dependency("test", "1.0.0", DependencyScope.TEST)
                .install(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(path).resolve("dependencies"))
            .content()
            .isEqualTo("dependency-1.0.0");
    }

    @Test
    void givenSchematicRequiresCommonDependency_whenConstructToStage_thenDependencyIsUsedWithHighestVersion(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) {
        factory.repositoryBuilder()
            .superManual()
            .manual(builder -> builder.name("dependencies").version("1.0.0"))
            .jar("dependencies", builder -> builder.name("dependencies").version("1.0.0"))
            .manual(builder ->
                builder.name("first")
                    .version("1.0.0")
                    .dependency("dependency", "1.0.0", DependencyScope.IMPLEMENTATION)
            )
            .jar("dependency", builder -> builder.name("first").version("1.0.0"))
            .manual(builder ->
                builder.name("second")
                    .version("1.0.0")
                    .dependency("dependency", "2.0.0", DependencyScope.IMPLEMENTATION)
            )
            .jar("dependency", builder -> builder.name("second").version("1.0.0"))
            .manual(builder -> builder.name("dependency").version("1.0.0"))
            .manual(builder -> builder.name("dependency").version("2.0.0"))
            .jar("dependency", builder -> builder.name("dependency").version("2.0.0"))
            .install(path);

        module.construct(
            factory.schematicBuilder()
                .name("project")
                .version("1.0.0")
                .repository("main", path, true)
                .plugin("dependencies", "1.0.0", Map.of())
                .dependency("first", "1.0.0", DependencyScope.IMPLEMENTATION)
                .dependency("second", "1.0.0", DependencyScope.IMPLEMENTATION)
                .install(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(path).resolve("dependencies"))
            .content(StandardCharsets.UTF_8)
            .hasLineCount(3)
            .contains("first-1.0.0", "second-1.0.0", "dependency-2.0.0");
    }

    @Test
    void givenHighestDependencyVersionRequiredByExcludedDependency_whenConstructToStage_thenDependencyIsUsedWithLowerVersion(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) {
        factory.repositoryBuilder()
            .superManual()
            .manual(builder -> builder.name("dependencies").version("1.0.0"))
            .jar("dependencies", builder -> builder.name("dependencies").version("1.0.0"))
            .manual(builder ->
                builder.name("first")
                    .version("1.0.0")
                    .dependency("should-not-be-affected", "1.0.0", DependencyScope.IMPLEMENTATION)
                    .dependency("exclude-affecting", "1.0.0", DependencyScope.IMPLEMENTATION)
            )
            .jar("dependency", builder -> builder.name("first").version("1.0.0"))
            .manual(builder ->
                builder.name("second")
                    .version("1.0.0")
                    .dependency("will-affect", "1.0.0", DependencyScope.IMPLEMENTATION)
            )
            .jar("dependency", builder -> builder.name("second").version("1.0.0"))
            .manual(builder -> builder.name("should-not-be-affected").version("1.0.0"))
            .jar("dependency", builder -> builder.name("should-not-be-affected").version("1.0.0"))
            .manual(builder ->
                builder.name("exclude-affecting")
                    .version("1.0.0")
                    .dependency("will-affect", "2.0.0", DependencyScope.IMPLEMENTATION)
            )
            .jar("dependency", builder -> builder.name("exclude-affecting").version("1.0.0"))
            .manual(builder ->
                builder.name("will-affect")
                    .version("1.0.0")
                    .dependency("should-not-be-affected", "2.0.0", DependencyScope.IMPLEMENTATION)
            )
            .manual(builder -> builder.name("should-not-be-affected").version("2.0.0"))
            .manual(builder -> builder.name("will-affect").version("2.0.0"))
            .jar("dependency", builder -> builder.name("will-affect").version("2.0.0"))
            .install(path);

        module.construct(
            factory.schematicBuilder()
                .name("project")
                .version("1.0.0")
                .repository("main", path, true)
                .plugin("dependencies", "1.0.0", Map.of())
                .dependency("first", "1.0.0", DependencyScope.IMPLEMENTATION)
                .dependency("second", "1.0.0", DependencyScope.IMPLEMENTATION)
                .install(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(path).resolve("dependencies"))
            .content()
            .hasLineCount(5)
            .contains(
                "first-1.0.0",
                "second-1.0.0",
                "should-not-be-affected-1.0.0",
                "exclude-affecting-1.0.0",
                "will-affect-2.0.0"
            );
    }

    @Test
    void givenPreferencesContainTransitiveDependency_whenConstructToStage_thenTransitiveDependencyVersionIsFromPreferences(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) {
        factory.repositoryBuilder()
            .superManual()
            .manual(builder -> builder.name("dependencies").version("1.0.0"))
            .jar("dependencies", builder -> builder.name("dependencies").version("1.0.0"))
            .manual(builder ->
                builder.name("dependency")
                    .version("1.0.0")
                    .dependency("transitive", "1.0.0", DependencyScope.IMPLEMENTATION)
            )
            .jar("dependency", builder -> builder.name("dependency").version("1.0.0"))
            .manual(builder -> builder.name("transitive").version("2.0.0"))
            .jar("dependency", builder -> builder.name("transitive").version("2.0.0"))
            .install(path);

        module.construct(
            factory.schematicBuilder()
                .name("project")
                .version("1.0.0")
                .repository("main", path, true)
                .plugin("dependencies", "1.0.0", Map.of())
                .preference("transitive", "2.0.0")
                .dependency("dependency", "1.0.0", DependencyScope.IMPLEMENTATION)
                .install(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(path).resolve("dependencies"))
            .content()
            .hasLineCount(2)
            .contains("dependency-1.0.0", "transitive-2.0.0");
    }

    @Test
    void givenPluginWithoutVersion_whenConstructToStage_thenVersionIsFromPreferences(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) {
        factory.repositoryBuilder()
            .superManual()
            .manual(builder -> builder.name("instant").version("1.0.0"))
            .jar("instant", builder -> builder.name("instant").version("1.0.0"))
            .install(path);

        module.construct(
            factory.schematicBuilder()
                .name("project")
                .version("1.0.0")
                .repository("main", path, true)
                .preference("instant", "1.0.0")
                .plugin("instant", Map.of("instant", "COMPILE-RUN"))
                .install(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(path).resolve("instant")).exists();
    }

    @Test
    void givenDirectDependencyWithoutVersion_whenConstructToStage_thenVersionIsFromPreferences(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) {
        factory.repositoryBuilder()
            .superManual()
            .manual(builder -> builder.name("dependencies").version("1.0.0"))
            .jar("dependencies", builder -> builder.name("dependencies").version("1.0.0"))
            .manual(builder -> builder.name("dependency").version("1.0.0"))
            .jar("dependency", builder -> builder.name("dependency").version("1.0.0"))
            .install(path);

        module.construct(
            factory.schematicBuilder()
                .name("project")
                .version("1.0.0")
                .repository("main", path, true)
                .preference("dependency", "1.0.0")
                .plugin("dependencies", "1.0.0", Map.of())
                .dependency("dependency", DependencyScope.IMPLEMENTATION)
                .install(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(path).resolve("dependencies"))
            .content()
            .isEqualTo("dependency-1.0.0");
    }

    @Test
    void givenPreferenceInclusion_whenConstructToStage_thenPreferencesAreIncludedFromReferencedManual(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) {
        factory.repositoryBuilder()
            .superManual()
            .manual(builder -> builder.name("instant").version("1.0.0"))
            .jar("instant", builder -> builder.name("instant").version("1.0.0"))
            .manual(builder ->
                builder.name("bom")
                    .version("1.0.0")
                    .preference("instant", "1.0.0")
            )
            .install(path);

        module.construct(
            factory.schematicBuilder()
                .name("project")
                .version("1.0.0")
                .repository("main", path, true)
                .preferenceInclusion("bom", "1.0.0")
                .plugin("instant", Map.of("instant", "COMPILE-RUN"))
                .install(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(path).resolve("instant")).exists();
    }

    @Test
    void givenTransitivePreferenceInclusion_whenConstructToStage_thenPreferencesAreIncludedFromTransitiveInclusion(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) {
        factory.repositoryBuilder()
            .superManual()
            .manual(builder -> builder.name("instant").version("1.0.0"))
            .jar("instant", builder -> builder.name("instant").version("1.0.0"))
            .manual(builder ->
                builder.name("bom")
                    .version("1.0.0")
                    .preferenceInclusion("parent-bom", "1.0.0")
            )
            .manual(builder ->
                builder.name("parent-bom")
                    .version("1.0.0")
                    .preference("instant", "1.0.0")
            )
            .install(path);

        module.construct(
            factory.schematicBuilder()
                .name("project")
                .version("1.0.0")
                .repository("main", path, true)
                .preferenceInclusion("bom", "1.0.0")
                .plugin("instant", Map.of("instant", "COMPILE-RUN"))
                .install(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(path).resolve("instant")).exists();
    }

    @ParameterizedTest
    @CsvSource(
        //https://semver.org
        textBlock = """
                    1.0.0, 2.0.0
                    2.0.0, 2.1.0
                    2.1.0, 2.1.1
                    1.0.0-alpha, 1.0.0
                    1.0.0-alpha, 1.0.0-alpha.1
                    1.0.0-alpha.1, 1.0.0-alpha.beta
                    1.0.0-alpha.beta, 1.0.0-beta
                    1.0.0-beta, 1.0.0-beta.2
                    1.0.0-beta.2, 1.0.0-beta.11
                    1.0.0-beta.11, 1.0.0-rc.1
                    """
    )
    void givenTransitiveDependency_whenConstructToStage_thenDependencyWithHigherSemanticVersionIsUsed(
        String lower,
        String higher,
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) {
        factory.repositoryBuilder()
            .superManual()
            .manual(builder -> builder.name("dependencies").version("1.0.0"))
            .jar("dependencies", builder -> builder.name("dependencies").version("1.0.0"))
            .manual(builder ->
                builder.name("dependency")
                    .version("1.0.0")
                    .dependency("common", higher, DependencyScope.IMPLEMENTATION)
            )
            .jar("dependency", builder -> builder.name("dependency").version("1.0.0"))
            .manual(builder -> builder.name("common").version(higher))
            .jar("dependency", builder -> builder.name("common").version(higher))
            .manual(builder -> builder.name("common").version(lower))
            .install(path);

        module.construct(
            factory.schematicBuilder()
                .name("project")
                .version("1.0.0")
                .repository("main", path, true)
                .plugin("dependencies", "1.0.0", Map.of())
                .dependency("dependency", "1.0.0", DependencyScope.IMPLEMENTATION)
                .dependency("common", lower, DependencyScope.IMPLEMENTATION)
                .install(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(path).resolve("dependencies"))
            .content(StandardCharsets.UTF_8)
            .hasLineCount(2)
            .contains("dependency-1.0.0", "common-" + higher);
    }

    @Test
    void givenInterpolatedArtifactDependencyVersion_whenConstructToStage_thenDependencyWithVersionFromPropertyIsUsed(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) {
        factory.repositoryBuilder()
            .superManual()
            .manual(builder -> builder.name("dependencies").version("1.0.0"))
            .jar("dependencies", builder -> builder.name("dependencies").version("1.0.0"))
            .manual(builder -> builder.name("dependency").version("1.0.0"))
            .jar("dependency", builder -> builder.name("dependency").version("1.0.0"))
            .install(path);

        module.construct(
            factory.schematicBuilder()
                .name("project")
                .version("1.0.0")
                .repository("main", path, true)
                .property("dependency.version", "1.0.0")
                .plugin("dependencies", "1.0.0", Map.of())
                .dependency("dependency", "${dependency.version}", DependencyScope.IMPLEMENTATION)
                .install(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(path).resolve("dependencies"))
            .content(StandardCharsets.UTF_8)
            .isEqualTo("dependency-1.0.0");
    }

    @Test
    void givenInterpolatedArtifactDependencyVersionInSchematicDependency_whenConstructToStage_thenDependencyWithVersionFromOtherSchematicPropertyIsUsed(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) {
        factory.repositoryBuilder()
            .superManual()
            .manual(builder -> builder.name("dependencies").version("1.0.0"))
            .jar("dependencies", builder -> builder.name("dependencies").version("1.0.0"))
            .manual(builder -> builder.name("product").version("1.0.0"))
            .jar("product", builder -> builder.name("product").version("1.0.0"))
            .manual(builder -> builder.name("library").version("1.0.0"))
            .jar("dependency", builder -> builder.name("library").version("1.0.0"))
            .jar("dependency", builder -> builder.name("dependency").version("1.0.0"))
            .install(path);
        var depends = path.resolve("depends");

        module.construct(
            factory.schematicBuilder()
                .name("project")
                .version("1.0.0")
                .repository("main", path, true)
                .inclusion(
                    factory.schematicBuilder()
                        .name("dependency")
                        .version("1.0.0")
                        .property("library.version", "1.0.0")
                        .plugin(
                            "product",
                            "1.0.0",
                            Map.of("path", path.resolve("dependency-1.0.0.jar").toString())
                        )
                        .dependency("library", "${library.version}", DependencyScope.IMPLEMENTATION)
                        .install(path.resolve("dependency"))
                )
                .inclusion(
                    factory.schematicBuilder()
                        .name("depends")
                        .version("1.0.0")
                        .plugin("dependencies", "1.0.0", Map.of())
                        .schematicDependency("dependency", DependencyScope.IMPLEMENTATION)
                        .install(depends)
                )
                .install(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(depends).resolve("dependencies"))
            .content(StandardCharsets.UTF_8)
            .hasLineCount(2)
            .contains("dependency-1.0.0", "library-1.0.0");
    }

    @Test
    void givenInterpolatedPluginVersion_whenConstructToStage_thenPluginWithVersionFromPropertyIsUsed(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) {
        factory.repositoryBuilder()
            .superManual()
            .manual(builder -> builder.name("instant").version("1.0.0"))
            .jar("instant", builder -> builder.name("instant").version("1.0.0"))
            .install(path);

        module.construct(
            factory.schematicBuilder()
                .name("project")
                .version("1.0.0")
                .repository("main", path, true)
                .property("instant.version", "1.0.0")
                .plugin("instant", "${instant.version}", Map.of("instant", "COMPILE-RUN"))
                .install(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(path).resolve("instant")).exists();
    }

    @Test
    void givenInterpolatedArtifactPreferenceVersion_whenConstructToStage_thenPluginWithVersionFromPropertyIsUsed(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) {
        factory.repositoryBuilder()
            .superManual()
            .manual(builder -> builder.name("instant").version("1.0.0"))
            .jar("instant", builder -> builder.name("instant").version("1.0.0"))
            .install(path);

        module.construct(
            factory.schematicBuilder()
                .name("project")
                .version("1.0.0")
                .repository("main", path, true)
                .property("instant.version", "1.0.0")
                .preference("instant", "${instant.version}")
                .plugin("instant", Map.of("instant", "COMPILE-RUN"))
                .install(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(path).resolve("instant")).exists();
    }

    @Test
    void givenInterpolatedPreferenceInclusionVersion_whenConstructToStage_thenPluginWithVersionFromPropertyIsUsed(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) {
        factory.repositoryBuilder()
            .superManual()
            .manual(builder -> builder.name("instant").version("1.0.0"))
            .jar("instant", builder -> builder.name("instant").version("1.0.0"))
            .manual(builder ->
                builder.name("bom")
                    .version("1.0.0")
                    .preference("instant", "1.0.0")
            )
            .install(path);

        module.construct(
            factory.schematicBuilder()
                .name("project")
                .version("1.0.0")
                .repository("main", path, true)
                .property("bom.version", "1.0.0")
                .preferenceInclusion("bom", "${bom.version}")
                .plugin("instant", Map.of("instant", "COMPILE-RUN"))
                .install(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(path).resolve("instant")).exists();
    }
}

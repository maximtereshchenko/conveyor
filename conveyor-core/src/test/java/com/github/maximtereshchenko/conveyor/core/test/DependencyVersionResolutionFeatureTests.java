package com.github.maximtereshchenko.conveyor.core.test;

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
    ) throws Exception {
        factory.repositoryBuilder(path)
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("class-path")
                    .dependency("dependency")
                    .dependency(
                        "group",
                        "test",
                        "1.0.0",
                        DependencyScope.TEST
                    )
            )
            .jar(
                factory.jarBuilder("class-path", path)
            )
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("dependency")
            )
            .jar(
                factory.jarBuilder("dependency", path)
            )
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("test")
                    .dependency(
                        "group",
                        "dependency",
                        "2.0.0",
                        DependencyScope.IMPLEMENTATION
                    )
            )
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("dependency")
                    .version("2.0.0")
            )
            .install(path);

        module.construct(
            factory.schematicDefinitionBuilder()
                .repository(path)
                .plugin("class-path")
                .conveyorJson(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(path).resolve("class-path"))
            .content(StandardCharsets.UTF_8)
            .isEqualTo("group-dependency-1.0.0");
    }

    @Test
    void givenPluginsRequireCommonDependency_whenConstructToStage_thenDependencyIsUsedWithHighestVersion(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) throws Exception {
        factory.repositoryBuilder(path)
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("first")
                    .dependency("dependency")
            )
            .jar(
                factory.jarBuilder("class-path", path)
                    .name("first")
            )
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("second")
                    .dependency(
                        "group",
                        "dependency",
                        "2.0.0",
                        DependencyScope.IMPLEMENTATION
                    )
            )
            .jar(
                factory.jarBuilder("class-path", path)
                    .name("second")
            )
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("dependency")
            )
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("dependency")
                    .version("2.0.0")
            )
            .jar(
                factory.jarBuilder("dependency", path)
                    .version("2.0.0")
            )
            .install(path);

        module.construct(
            factory.schematicDefinitionBuilder()
                .repository(path)
                .plugin("first")
                .plugin("second")
                .conveyorJson(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(path).resolve("class-path"))
            .content(StandardCharsets.UTF_8)
            .isEqualTo("group-dependency-2.0.0");
    }

    @Test
    void givenHighestDependencyVersionRequiredByExcludedDependency_whenConstructToStage_thenPluginUsesLowerVersionDependency(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) throws Exception {
        factory.repositoryBuilder(path)
            .schematicDefinition(
                factory.schematicDefinitionBuilder().name("first")
                    .dependency("should-not-be-affected")
                    .dependency("exclude-affecting")
            )
            .jar(
                factory.jarBuilder("class-path", path)
                    .name("first")
            )
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("second")
                    .dependency("will-affect")
            )
            .jar(
                factory.jarBuilder("class-path", path)
                    .name("second")
            )
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("should-not-be-affected")
            )
            .jar(
                factory.jarBuilder("dependency", path)
                    .name("should-not-be-affected")
            )
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("exclude-affecting")
                    .dependency(
                        "group",
                        "will-affect",
                        "2.0.0",
                        DependencyScope.IMPLEMENTATION
                    )
            )
            .jar(
                factory.jarBuilder("dependency", path)
                    .name("exclude-affecting")

            )
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("will-affect")
                    .dependency(
                        "group",
                        "should-not-be-affected",
                        "2.0.0",
                        DependencyScope.IMPLEMENTATION
                    )
            )
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("should-not-be-affected")
                    .version("2.0.0")
            )
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("will-affect")
                    .version("2.0.0")
            )
            .jar(
                factory.jarBuilder("dependency", path)
                    .name("will-affect")
                    .version("2.0.0")
            )
            .install(path);

        module.construct(
            factory.schematicDefinitionBuilder()
                .repository(path)
                .plugin("first")
                .plugin("second")
                .conveyorJson(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(path).resolve("class-path"))
            .content(StandardCharsets.UTF_8)
            .hasLineCount(3)
            .contains(
                "should-not-be-affected-1.0.0",
                "exclude-affecting-1.0.0",
                "will-affect-2.0.0"
            );
    }

    @Test
    void givenTestDependencyRequireOtherDependencyHigherVersion_whenConstructToStage_thenOtherDependencyIsUsedWithLowerVersion(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) throws Exception {
        factory.repositoryBuilder(path)
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("dependencies")
            )
            .jar(
                factory.jarBuilder("dependencies", path)
            )
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("dependency")
            )
            .jar(
                factory.jarBuilder("dependency", path)
            )
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("test")
                    .dependency(
                        "group",
                        "dependency",
                        "2.0.0",
                        DependencyScope.IMPLEMENTATION
                    )
            )
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("dependency")
                    .version("2.0.0")
            )
            .install(path);

        module.construct(
            factory.schematicDefinitionBuilder()
                .repository(path)
                .plugin("dependencies")
                .dependency("dependency")
                .dependency(
                    "group",
                    "test",
                    "1.0.0",
                    DependencyScope.TEST
                )
                .conveyorJson(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(path).resolve("dependencies"))
            .content()
            .isEqualTo("group-dependency-1.0.0");
    }

    @Test
    void givenSchematicRequiresCommonDependency_whenConstructToStage_thenDependencyIsUsedWithHighestVersion(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) throws Exception {
        factory.repositoryBuilder(path)
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("dependencies")
            )
            .jar(
                factory.jarBuilder("dependencies", path)
            )
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("first")
                    .dependency("dependency")
            )
            .jar(
                factory.jarBuilder("dependency", path)
                    .name("first")
            )
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("second")
                    .dependency(
                        "group",
                        "dependency",
                        "2.0.0",
                        DependencyScope.IMPLEMENTATION
                    )
            )
            .jar(
                factory.jarBuilder("dependency", path)
                    .name("second")
            )
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("dependency")
            )
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("dependency")
                    .version("2.0.0")
            )
            .jar(
                factory.jarBuilder("dependency", path)
                    .name("dependency")
                    .version("2.0.0")
            )
            .install(path);

        module.construct(
            factory.schematicDefinitionBuilder()
                .repository(path)
                .plugin("dependencies")
                .dependency("first")
                .dependency("second")
                .conveyorJson(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(path).resolve("dependencies"))
            .content(StandardCharsets.UTF_8)
            .hasLineCount(3)
            .contains("group-first-1.0.0", "second-1.0.0", "group-dependency-2.0.0");
    }

    @Test
    void givenHighestDependencyVersionRequiredByExcludedDependency_whenConstructToStage_thenDependencyIsUsedWithLowerVersion(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) throws Exception {
        factory.repositoryBuilder(path)
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("dependencies")
            )
            .jar(
                factory.jarBuilder("dependencies", path)
            )
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("first")
                    .dependency("should-not-be-affected")
                    .dependency("exclude-affecting")
            )
            .jar(
                factory.jarBuilder("dependency", path)
                    .name("first")
            )
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("second")
                    .dependency("will-affect")
            )
            .jar(
                factory.jarBuilder("dependency", path)
                    .name("second")
            )
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("should-not-be-affected")
            )
            .jar(
                factory.jarBuilder("dependency", path)
                    .name("should-not-be-affected")
            )
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("exclude-affecting")
                    .dependency(
                        "group",
                        "will-affect",
                        "2.0.0",
                        DependencyScope.IMPLEMENTATION
                    )
            )
            .jar(
                factory.jarBuilder("dependency", path)
                    .name("exclude-affecting")
            )
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("will-affect")
                    .dependency(
                        "group",
                        "should-not-be-affected",
                        "2.0.0",
                        DependencyScope.IMPLEMENTATION
                    )
            )
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("should-not-be-affected")
                    .version("2.0.0")
            )
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("will-affect")
                    .version("2.0.0")
            )
            .jar(
                factory.jarBuilder("dependency", path)
                    .name("will-affect")
                    .version("2.0.0")
            )
            .install(path);

        module.construct(
            factory.schematicDefinitionBuilder()

                .repository(path)
                .plugin("dependencies")
                .dependency("first")
                .dependency("second")
                .conveyorJson(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(path).resolve("dependencies"))
            .content()
            .hasLineCount(5)
            .contains(
                "group-first-1.0.0",
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
    ) throws Exception {
        factory.repositoryBuilder(path)
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("dependencies")
            )
            .jar(
                factory.jarBuilder("dependencies", path)
            )
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("dependency")
                    .dependency("transitive")
            )
            .jar(
                factory.jarBuilder("dependency", path)
            )
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("transitive")
                    .version("2.0.0")
            )
            .jar(
                factory.jarBuilder("dependency", path)
                    .name("transitive")
                    .version("2.0.0")
            )
            .install(path);

        module.construct(
            factory.schematicDefinitionBuilder()

                .repository(path)
                .plugin("dependencies")
                .preference("transitive", "2.0.0")
                .dependency("dependency")
                .conveyorJson(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(path).resolve("dependencies"))
            .content()
            .hasLineCount(2)
            .contains("group-dependency-1.0.0", "transitive-2.0.0");
    }

    @Test
    void givenPluginWithoutVersion_whenConstructToStage_thenVersionIsFromPreferences(
        @TempDir Path path,
        ConveyorModule module,
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

        module.construct(
            factory.schematicDefinitionBuilder()
                .repository(path)
                .preference("instant", "1.0.0")
                .plugin(
                    "group",
                    "instant",
                    null,
                    Map.of("instant", "COMPILE-RUN")
                )
                .conveyorJson(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(path).resolve("instant")).exists();
    }

    @Test
    void givenDirectDependencyWithoutVersion_whenConstructToStage_thenVersionIsFromPreferences(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) throws Exception {
        factory.repositoryBuilder(path)
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("dependencies")
            )
            .jar(
                factory.jarBuilder("dependencies", path)
            )
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("dependency")
            )
            .jar(
                factory.jarBuilder("dependency", path)
            )
            .install(path);

        module.construct(
            factory.schematicDefinitionBuilder()
                .repository(path)
                .preference("dependency", "1.0.0")
                .plugin("dependencies")
                .dependency(
                    "group",
                    "dependency",
                    null,
                    DependencyScope.IMPLEMENTATION
                )
                .conveyorJson(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(path).resolve("dependencies"))
            .content()
            .isEqualTo("group-dependency-1.0.0");
    }

    @Test
    void givenPreferenceInclusion_whenConstructToStage_thenPreferencesAreIncludedFromReferencedSchematic(
        @TempDir Path path,
        ConveyorModule module,
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
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("bom")
                    .preference("instant", "1.0.0")
            )
            .install(path);

        module.construct(
            factory.schematicDefinitionBuilder()
                .repository(path)
                .preferenceInclusion("bom")
                .plugin(
                    "group",
                    "instant",
                    null,
                    Map.of("instant", "COMPILE-RUN")
                )
                .conveyorJson(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(path).resolve("instant")).exists();
    }

    @Test
    void givenTransitivePreferenceInclusion_whenConstructToStage_thenPreferencesAreIncludedFromTransitiveInclusion(
        @TempDir Path path,
        ConveyorModule module,
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
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("bom")
                    .preferenceInclusion("parent-bom")
            )
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("parent-bom")
                    .preference("instant", "1.0.0")
            )
            .install(path);

        module.construct(
            factory.schematicDefinitionBuilder()

                .repository(path)
                .preferenceInclusion("bom")
                .plugin(
                    "group",
                    "instant",
                    null,
                    Map.of("instant", "COMPILE-RUN")
                )
                .conveyorJson(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(path).resolve("instant")).exists();
    }

    @ParameterizedTest
    @CsvSource(
        //https://semver.org
        //https://github.com/apache/maven/blob/804cfea8f69ab217e6b06055cb4b9bcbfcd4619a/maven-artifact/src/test/java/org/apache/maven/artifact/versioning/ComparableVersionTest.java
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
                    1.0, 1.0.1
                    1.0-alpha, 1.0
                    1, 1.1
                    1-alpha2, 1-alpha-123
                    1-alpha-123, 1-beta-2
                    1-beta-2, 1-beta123
                    1-beta123, 1-m2
                    1-m2, 1-m11
                    1-m11, 1-rc
                    1-cr2, 1-rc123
                    1-rc123, 1-SNAPSHOT
                    1-SNAPSHOT, 1
                    1-sp, 1-sp2
                    1-sp2, 1-sp123
                    1-abc, 1-def
                    1-def, 1-pom-1
                    1-1, 1-2
                    1-2, 1-123
                    2.0, 2.0.a
                    2-1, 2.0.2
                    2.0.2, 2.0.123
                    2.0.123, 2.1.0
                    2.1-a, 2.1b
                    2.1-1, 2.1.0.1
                    2.1.0.1, 2.2
                    2.2, 2.123
                    2.123, 11.a2
                    11.a2, 11.a11
                    11.a11, 11.b2
                    11.b2, 11.b11
                    11.b11, 11.m2
                    11.m2, 11.m11
                    11, 11.a
                    11.a, 11b
                    11b, 11c
                    11c, 11m
                    """
    )
    void givenTransitiveDependency_whenConstructToStage_thenDependencyWithHigherSemanticVersionIsUsed(
        String lower,
        String higher,
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) throws Exception {
        factory.repositoryBuilder(path)
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("dependencies")
            )
            .jar(
                factory.jarBuilder("dependencies", path)
            )
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("dependency")
                    .dependency(
                        "group",
                        "common",
                        higher,
                        DependencyScope.IMPLEMENTATION
                    )
            )
            .jar(
                factory.jarBuilder("dependency", path)
            )
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("common")
                    .version(higher)
            )
            .jar(
                factory.jarBuilder("dependency", path)
                    .name("common")
                    .version(higher)
            )
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("common")
                    .version(lower)
            )
            .install(path);

        module.construct(
            factory.schematicDefinitionBuilder()
                .repository(path)
                .plugin("dependencies")
                .dependency("dependency")
                .dependency(
                    "group",
                    "common",
                    lower,
                    DependencyScope.IMPLEMENTATION
                )
                .conveyorJson(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(path).resolve("dependencies"))
            .content(StandardCharsets.UTF_8)
            .hasLineCount(2)
            .contains("group-dependency-1.0.0", "common-" + higher);
    }

    @Test
    void givenPreferenceIncludedWithMultipleVersions_whenConstructToStage_thenPreferenceWithHighestVersionIsUsed(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) throws Exception {
        factory.repositoryBuilder(path)
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("instant")
                    .version("2.0.0")
            )
            .jar(
                factory.jarBuilder("instant", path)
                    .version("2.0.0")
            )
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("bom-with-lower-version")
                    .preference("instant", "1.0.0")
            )
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("bom-with-higher-version")
                    .preference("instant", "2.0.0")
            )
            .install(path);

        module.construct(
            factory.schematicDefinitionBuilder()
                .repository(path)
                .preferenceInclusion("bom-with-higher-version")
                .preferenceInclusion("bom-with-lower-version")
                .plugin(
                    "group",
                    "instant",
                    null,
                    Map.of("instant", "COMPILE-RUN")
                )
                .conveyorJson(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(path).resolve("instant")).exists();
    }

    @Test
    void givenPreferenceFromInclusionDefined_whenConstructToStage_thenPreferenceWithDefinedVersionIsUsed(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) throws Exception {
        factory.repositoryBuilder(path)
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("instant")
                    .version("1.0.0")
            )
            .jar(
                factory.jarBuilder("instant", path)
                    .version("1.0.0")
            )
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("bom")
                    .preference("instant", "2.0.0")
            )
            .install(path);

        module.construct(
            factory.schematicDefinitionBuilder()
                .repository(path)
                .preferenceInclusion("bom")
                .preference("instant", "1.0.0")
                .plugin(
                    "group",
                    "instant",
                    null,
                    Map.of("instant", "COMPILE-RUN")
                )
                .conveyorJson(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(path).resolve("instant")).exists();
    }

    @Test
    void givenNoTransitiveDependencyVersion_whenConstructToStage_thenVersionFromDependencySchematicPreferencesIsUsed(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) throws Exception {
        factory.repositoryBuilder(path)
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("dependency")
                    .preference("transitive", "1.0.0")
                    .dependency(
                        "group",
                        "transitive",
                        null,
                        DependencyScope.IMPLEMENTATION
                    )
            )
            .jar(
                factory.jarBuilder("dependency", path)
                    .name("dependency")
            )
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("transitive")
            )
            .jar(
                factory.jarBuilder("dependency", path)
                    .name("transitive")
            )
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("dependencies")
            )
            .jar(
                factory.jarBuilder("dependencies", path)
            )
            .install(path);

        module.construct(
            factory.schematicDefinitionBuilder()
                .repository(path)
                .plugin("dependencies")
                .dependency("dependency")
                .conveyorJson(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(path).resolve("dependencies"))
            .content()
            .hasLineCount(2)
            .contains("group-dependency-1.0.0", "transitive-1.0.0");
    }
}

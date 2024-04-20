package com.github.maximtereshchenko.conveyor.core.test;

import com.github.maximtereshchenko.conveyor.api.ConveyorModule;
import com.github.maximtereshchenko.conveyor.common.api.DependencyScope;
import com.github.maximtereshchenko.conveyor.common.api.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

final class DependencyVersionResolutionFeatureTests extends ConveyorTest {

    @Test
    void givenTestDependencyRequireHigherVersion_whenConstructToStage_thenDependencyIsUsedWithLowerVersion(
        Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) throws Exception {
        factory.repositoryBuilder()
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("module-path")
                    .dependency("dependency")
                    .dependency(
                        "test",
                        "1.0.0",
                        DependencyScope.TEST
                    )
            )
            .jar(
                factory.jarBuilder("module-path")
            )
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("dependency")
            )
            .jar(
                factory.jarBuilder("dependency")
            )
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("test")
                    .dependency(
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
                .plugin("module-path")
                .conveyorJson(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(path).resolve("module-path"))
            .content(StandardCharsets.UTF_8)
            .isEqualTo("dependency-1.0.0");
    }

    @Test
    void givenPluginsRequireCommonDependency_whenConstructToStage_thenDependencyIsUsedWithHighestVersion(
        Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) throws Exception {
        factory.repositoryBuilder()
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("first")
                    .dependency("dependency")
            )
            .jar(
                factory.jarBuilder("module-path")
                    .name("first")
            )
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("second")
                    .dependency(
                        "dependency",
                        "2.0.0",
                        DependencyScope.IMPLEMENTATION
                    )
            )
            .jar(
                factory.jarBuilder("module-path")
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
                factory.jarBuilder("dependency")
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

        assertThat(defaultConstructionDirectory(path).resolve("module-path"))
            .content(StandardCharsets.UTF_8)
            .isEqualTo("dependency-2.0.0");
    }

    @Test
    void givenHighestDependencyVersionRequiredByExcludedDependency_whenConstructToStage_thenPluginUsesLowerVersionDependency(
        Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) throws Exception {
        factory.repositoryBuilder()
            .schematicDefinition(
                factory.schematicDefinitionBuilder().name("first")
                    .dependency("should-not-be-affected")
                    .dependency("exclude-affecting")
            )
            .jar(
                factory.jarBuilder("module-path")
                    .name("first")
            )
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("second")
                    .dependency("will-affect")
            )
            .jar(
                factory.jarBuilder("module-path")
                    .name("second")
            )
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("should-not-be-affected")
            )
            .jar(
                factory.jarBuilder("dependency")
                    .name("should-not-be-affected")
            )
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("exclude-affecting")
                    .dependency(
                        "will-affect",
                        "2.0.0",
                        DependencyScope.IMPLEMENTATION
                    )
            )
            .jar(
                factory.jarBuilder("dependency")
                    .name("exclude-affecting")

            )
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("will-affect")
                    .dependency(
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
                factory.jarBuilder("dependency")
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

        assertThat(defaultConstructionDirectory(path).resolve("module-path"))
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
        Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) throws Exception {
        factory.repositoryBuilder()
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("dependencies")
            )
            .jar(
                factory.jarBuilder("dependencies")
            )
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("dependency")
            )
            .jar(
                factory.jarBuilder("dependency")
            )
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("test")
                    .dependency(
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
                .dependency("test", "1.0.0", DependencyScope.TEST)
                .conveyorJson(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(path).resolve("dependencies"))
            .content()
            .isEqualTo("dependency-1.0.0");
    }

    @Test
    void givenSchematicRequiresCommonDependency_whenConstructToStage_thenDependencyIsUsedWithHighestVersion(
        Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) throws Exception {
        factory.repositoryBuilder()
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("dependencies")
            )
            .jar(
                factory.jarBuilder("dependencies")
            )
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("first")
                    .dependency("dependency")
            )
            .jar(
                factory.jarBuilder("dependency")
                    .name("first")
            )
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("second")
                    .dependency(
                        "dependency",
                        "2.0.0",
                        DependencyScope.IMPLEMENTATION
                    )
            )
            .jar(
                factory.jarBuilder("dependency")
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
                factory.jarBuilder("dependency")
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
            .contains("first-1.0.0", "second-1.0.0", "dependency-2.0.0");
    }

    @Test
    void givenHighestDependencyVersionRequiredByExcludedDependency_whenConstructToStage_thenDependencyIsUsedWithLowerVersion(
        Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) throws Exception {
        factory.repositoryBuilder()
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("dependencies")
            )
            .jar(
                factory.jarBuilder("dependencies")
            )
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("first")
                    .dependency("should-not-be-affected")
                    .dependency("exclude-affecting")
            )
            .jar(
                factory.jarBuilder("dependency")
                    .name("first")
            )
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("second")
                    .dependency("will-affect")
            )
            .jar(
                factory.jarBuilder("dependency")
                    .name("second")
            )
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("should-not-be-affected")
            )
            .jar(
                factory.jarBuilder("dependency")
                    .name("should-not-be-affected")
            )
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("exclude-affecting")
                    .dependency(
                        "will-affect",
                        "2.0.0",
                        DependencyScope.IMPLEMENTATION
                    )
            )
            .jar(
                factory.jarBuilder("dependency")
                    .name("exclude-affecting")
            )
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("will-affect")
                    .dependency(
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
                factory.jarBuilder("dependency")
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
                "first-1.0.0",
                "second-1.0.0",
                "should-not-be-affected-1.0.0",
                "exclude-affecting-1.0.0",
                "will-affect-2.0.0"
            );
    }

    @Test
    void givenPreferencesContainTransitiveDependency_whenConstructToStage_thenTransitiveDependencyVersionIsFromPreferences(
        Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) throws Exception {
        factory.repositoryBuilder()
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("dependencies")
            )
            .jar(
                factory.jarBuilder("dependencies")
            )
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("dependency")
                    .dependency("transitive")
            )
            .jar(
                factory.jarBuilder("dependency")
            )
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("transitive")
                    .version("2.0.0")
            )
            .jar(
                factory.jarBuilder("dependency")
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
            .contains("dependency-1.0.0", "transitive-2.0.0");
    }

    @Test
    void givenPluginWithoutVersion_whenConstructToStage_thenVersionIsFromPreferences(
        Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) throws Exception {
        factory.repositoryBuilder()
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("instant")
            )
            .jar(
                factory.jarBuilder("instant")
            )
            .install(path);

        module.construct(
            factory.schematicDefinitionBuilder()
                .repository(path)
                .preference("instant", "1.0.0")
                .plugin("instant", Map.of("instant", "COMPILE-RUN"))
                .conveyorJson(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(path).resolve("instant")).exists();
    }

    @Test
    void givenDirectDependencyWithoutVersion_whenConstructToStage_thenVersionIsFromPreferences(
        Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) throws Exception {
        factory.repositoryBuilder()
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("dependencies")
            )
            .jar(
                factory.jarBuilder("dependencies")
            )
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("dependency")
            )
            .jar(
                factory.jarBuilder("dependency")
            )
            .install(path);

        module.construct(
            factory.schematicDefinitionBuilder()
                .repository(path)
                .preference("dependency", "1.0.0")
                .plugin("dependencies")
                .dependency("dependency", DependencyScope.IMPLEMENTATION)
                .conveyorJson(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(path).resolve("dependencies"))
            .content()
            .isEqualTo("dependency-1.0.0");
    }

    @Test
    void givenPreferenceInclusion_whenConstructToStage_thenPreferencesAreIncludedFromReferencedSchematic(
        Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) throws Exception {
        factory.repositoryBuilder()
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("instant")
            )
            .jar(
                factory.jarBuilder("instant")
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
                .plugin("instant", Map.of("instant", "COMPILE-RUN"))
                .conveyorJson(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(path).resolve("instant")).exists();
    }

    @Test
    void givenTransitivePreferenceInclusion_whenConstructToStage_thenPreferencesAreIncludedFromTransitiveInclusion(
        Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) throws Exception {
        factory.repositoryBuilder()
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("instant")
            )
            .jar(
                factory.jarBuilder("instant")
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
                .plugin("instant", Map.of("instant", "COMPILE-RUN"))
                .conveyorJson(path),
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
                    1.0, 1.0.1
                    """
    )
    void givenTransitiveDependency_whenConstructToStage_thenDependencyWithHigherSemanticVersionIsUsed(
        String lower,
        String higher,
        Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) throws Exception {
        factory.repositoryBuilder()
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("dependencies")
            )
            .jar(
                factory.jarBuilder("dependencies")
            )
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("dependency")
                    .dependency(
                        "common",
                        higher,
                        DependencyScope.IMPLEMENTATION
                    )
            )
            .jar(
                factory.jarBuilder("dependency")
            )
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("common")
                    .version(higher)
            )
            .jar(
                factory.jarBuilder("dependency")
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
                .dependency("common", lower, DependencyScope.IMPLEMENTATION)
                .conveyorJson(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(path).resolve("dependencies"))
            .content(StandardCharsets.UTF_8)
            .hasLineCount(2)
            .contains("dependency-1.0.0", "common-" + higher);
    }

    @Test
    void givenInterpolatedArtifactDependencyVersion_whenConstructToStage_thenDependencyWithVersionFromPropertyIsUsed(
        Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) throws Exception {
        factory.repositoryBuilder()
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("dependencies")
            )
            .jar(
                factory.jarBuilder("dependencies")
            )
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("dependency")
            )
            .jar(
                factory.jarBuilder("dependency")
            )
            .install(path);

        module.construct(
            factory.schematicDefinitionBuilder()
                .repository(path)
                .property("dependency.version", "1.0.0")
                .plugin("dependencies")
                .dependency(
                    "dependency",
                    "${dependency.version}",
                    DependencyScope.IMPLEMENTATION
                )
                .conveyorJson(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(path).resolve("dependencies"))
            .content(StandardCharsets.UTF_8)
            .isEqualTo("dependency-1.0.0");
    }

    @Test
    void givenInterpolatedArtifactDependencyVersionInSchematicDependency_whenConstructToStage_thenDependencyWithVersionFromOtherSchematicPropertyIsUsed(
        Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) throws Exception {
        factory.repositoryBuilder()
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("dependencies")
            )
            .jar(
                factory.jarBuilder("dependencies")
            )
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("product")
            )
            .jar(
                factory.jarBuilder("product")
            )
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("library")
            )
            .jar(
                factory.jarBuilder("dependency")
                    .name("library")
            )
            .jar(
                factory.jarBuilder("dependency")
            )
            .install(path);
        var depends = path.resolve("depends");

        module.construct(
            factory.schematicDefinitionBuilder()
                .repository(path)
                .inclusion(
                    factory.schematicDefinitionBuilder()
                        .name("dependency")
                        .template("project")
                        .property("library.version", "1.0.0")
                        .plugin(
                            "product",
                            "1.0.0",
                            Map.of(
                                "path",
                                path.resolve("com")
                                    .resolve("github")
                                    .resolve("maximtereshchenko")
                                    .resolve("conveyor")
                                    .resolve("dependency")
                                    .resolve("1.0.0")
                                    .resolve("dependency-1.0.0.jar")
                                    .toString()
                            )
                        )
                        .dependency(
                            "library",
                            "${library.version}",
                            DependencyScope.IMPLEMENTATION
                        )
                        .conveyorJson(path.resolve("dependency"))
                )
                .inclusion(
                    factory.schematicDefinitionBuilder()
                        .name("depends")
                        .template("project")
                        .plugin("dependencies", "1.0.0", Map.of())
                        .dependency("dependency")
                        .conveyorJson(depends)
                )
                .conveyorJson(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(depends).resolve("dependencies"))
            .content(StandardCharsets.UTF_8)
            .hasLineCount(2)
            .contains("dependency-1.0.0", "library-1.0.0");
    }

    @Test
    void givenInterpolatedPluginVersion_whenConstructToStage_thenPluginWithVersionFromPropertyIsUsed(
        Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) throws Exception {
        factory.repositoryBuilder()
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("instant")
            )
            .jar(
                factory.jarBuilder("instant")
            )
            .install(path);

        module.construct(
            factory.schematicDefinitionBuilder()
                .repository(path)
                .property("instant.version", "1.0.0")
                .plugin(
                    "instant",
                    "${instant.version}",
                    Map.of("instant", "COMPILE-RUN")
                )
                .conveyorJson(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(path).resolve("instant")).exists();
    }

    @Test
    void givenInterpolatedArtifactPreferenceVersion_whenConstructToStage_thenPluginWithVersionFromPropertyIsUsed(
        Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) throws Exception {
        factory.repositoryBuilder()
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("instant")
            )
            .jar(
                factory.jarBuilder("instant")
            )
            .install(path);

        module.construct(
            factory.schematicDefinitionBuilder()
                .repository(path)
                .property("instant.version", "1.0.0")
                .preference("instant", "${instant.version}")
                .plugin("instant", Map.of("instant", "COMPILE-RUN"))
                .conveyorJson(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(path).resolve("instant")).exists();
    }

    @Test
    void givenInterpolatedPreferenceInclusionVersion_whenConstructToStage_thenPluginWithVersionFromPropertyIsUsed(
        Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) throws Exception {
        factory.repositoryBuilder()
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("instant")
            )
            .jar(
                factory.jarBuilder("instant")
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
                .property("bom.version", "1.0.0")
                .preferenceInclusion("bom", "${bom.version}")
                .plugin("instant", Map.of("instant", "COMPILE-RUN"))
                .conveyorJson(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(path).resolve("instant")).exists();
    }

    @Test
    void givenPreferenceIncludedWithMultipleVersions_whenConstructToStage_thenPreferenceWithHighestVersionIsUsed(
        Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) throws Exception {
        factory.repositoryBuilder()
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("instant")
                    .version("2.0.0")
            )
            .jar(
                factory.jarBuilder("instant")
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
                .plugin("instant", Map.of("instant", "COMPILE-RUN"))
                .conveyorJson(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(path).resolve("instant")).exists();
    }

    @Test
    void givenPreferenceFromInclusionDefined_whenConstructToStage_thenPreferenceWithDefinedVersionIsUsed(
        Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) throws Exception {
        factory.repositoryBuilder()
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("instant")
                    .version("1.0.0")
            )
            .jar(
                factory.jarBuilder("instant")
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
                .plugin("instant", Map.of("instant", "COMPILE-RUN"))
                .conveyorJson(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(path).resolve("instant")).exists();
    }
}

package com.github.maximtereshchenko.conveyor.domain.test;

import com.github.maximtereshchenko.conveyor.common.api.DependencyScope;
import com.github.maximtereshchenko.conveyor.gson.GsonAdapter;
import java.nio.file.Path;
import java.util.Locale;

final class DependencyBuilder implements ArtifactBuilder {

    private final ProjectDefinitionBuilder projectDefinitionBuilder;
    private final DependencySourceCodeBuilder dependencySourceCodeBuilder;
    private final ModuleInfoSourceCodeBuilder moduleInfoSourceCodeBuilder;

    private DependencyBuilder(
        ProjectDefinitionBuilder projectDefinitionBuilder,
        DependencySourceCodeBuilder dependencySourceCodeBuilder,
        ModuleInfoSourceCodeBuilder moduleInfoSourceCodeBuilder
    ) {
        this.projectDefinitionBuilder = projectDefinitionBuilder;
        this.dependencySourceCodeBuilder = dependencySourceCodeBuilder;
        this.moduleInfoSourceCodeBuilder = moduleInfoSourceCodeBuilder;
    }

    static DependencyBuilder empty(GsonAdapter gsonAdapter) {
        var builder = ProjectDefinitionBuilder.empty(gsonAdapter, "dependency");
        return new DependencyBuilder(
            builder,
            new DependencySourceCodeBuilder(builder),
            new ModuleInfoSourceCodeBuilder(builder.name())
        );
    }

    @Override
    public int version() {
        return projectDefinitionBuilder.version();
    }

    @Override
    public String name() {
        return projectDefinitionBuilder.name();
    }

    @Override
    public Path install(Path path) {
        projectDefinitionBuilder.install(path);
        return new JarBuilder(
            projectDefinitionBuilder.name(),
            projectDefinitionBuilder.version(),
            dependencySourceCodeBuilder.build(),
            moduleInfoSourceCodeBuilder.build()
        )
            .install(path);
    }

    String fullyQualifiedName() {
        return normalizedName() + '.' + normalizedName();
    }

    String module() {
        return normalizedName();
    }

    DependencyBuilder name(String name) {
        return new DependencyBuilder(
            projectDefinitionBuilder.name(name),
            dependencySourceCodeBuilder.name(name),
            moduleInfoSourceCodeBuilder.name(name)
        );
    }

    DependencyBuilder version(int version) {
        return new DependencyBuilder(
            projectDefinitionBuilder.version(version),
            dependencySourceCodeBuilder.version(version),
            moduleInfoSourceCodeBuilder
        );
    }

    DependencyBuilder dependency(ProjectDefinitionBuilder builder) {
        return dependency(builder, DependencyScope.IMPLEMENTATION);
    }

    DependencyBuilder dependency(ProjectDefinitionBuilder builder, DependencyScope scope) {
        return new DependencyBuilder(
            projectDefinitionBuilder.dependency(builder, scope),
            dependencySourceCodeBuilder,
            moduleInfoSourceCodeBuilder
        );
    }

    DependencyBuilder dependency(DependencyBuilder builder) {
        return new DependencyBuilder(
            projectDefinitionBuilder.dependency(builder, DependencyScope.IMPLEMENTATION),
            dependencySourceCodeBuilder.dependency(builder.fullyQualifiedName()),
            moduleInfoSourceCodeBuilder.requires(builder.module())
        );
    }

    private String normalizedName() {
        return name().toLowerCase(Locale.ROOT).replace("-", "");
    }
}

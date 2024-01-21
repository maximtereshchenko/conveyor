package com.github.maximtereshchenko.conveyor.domain.test;

import com.github.maximtereshchenko.conveyor.common.api.DependencyScope;
import com.github.maximtereshchenko.conveyor.common.api.Stage;
import com.github.maximtereshchenko.conveyor.gson.GsonAdapter;

import java.nio.file.Path;

final class ConveyorPluginBuilder implements ArtifactBuilder {

    private final ProjectDefinitionBuilder projectDefinitionBuilder;
    private final ConveyorPluginSourceCodeBuilder conveyorPluginSourceCodeBuilder;
    private final ModuleInfoSourceCodeBuilder moduleInfoSourceCodeBuilder;

    private ConveyorPluginBuilder(
        ProjectDefinitionBuilder projectDefinitionBuilder,
        ConveyorPluginSourceCodeBuilder conveyorPluginSourceCodeBuilder,
        ModuleInfoSourceCodeBuilder moduleInfoSourceCodeBuilder
    ) {
        this.projectDefinitionBuilder = projectDefinitionBuilder;
        this.conveyorPluginSourceCodeBuilder = conveyorPluginSourceCodeBuilder;
        this.moduleInfoSourceCodeBuilder = moduleInfoSourceCodeBuilder;
    }

    static ConveyorPluginBuilder empty(GsonAdapter gsonAdapter) {
        var projectDefinitionBuilder = ProjectDefinitionBuilder.empty(gsonAdapter, "plugin");
        var conveyorPluginSourceCodeBuilder = new ConveyorPluginSourceCodeBuilder(projectDefinitionBuilder);
        return new ConveyorPluginBuilder(
            projectDefinitionBuilder,
            conveyorPluginSourceCodeBuilder,
            new ModuleInfoSourceCodeBuilder(projectDefinitionBuilder.name())
                .requires("com.github.maximtereshchenko.conveyor.plugin.api")
                .requires("com.github.maximtereshchenko.conveyor.common.api")
                .providesConveyorPlugin(conveyorPluginSourceCodeBuilder.fullyQualifiedName())
        );
    }

    @Override
    public String name() {
        return projectDefinitionBuilder.name();
    }

    @Override
    public int version() {
        return projectDefinitionBuilder.version();
    }

    @Override
    public Path install(Path path) {
        projectDefinitionBuilder.install(path);
        return new JarBuilder(
            projectDefinitionBuilder.name(),
            projectDefinitionBuilder.version(),
            conveyorPluginSourceCodeBuilder.build(),
            moduleInfoSourceCodeBuilder.build()
        )
            .install(path);
    }

    ConveyorPluginBuilder name(String name) {
        var builder = conveyorPluginSourceCodeBuilder.name(name);
        return new ConveyorPluginBuilder(
            projectDefinitionBuilder.name(name),
            builder,
            moduleInfoSourceCodeBuilder.name(name)
                .providesConveyorPlugin(builder.fullyQualifiedName())
        );
    }

    ConveyorPluginBuilder version(int version) {
        return new ConveyorPluginBuilder(
            projectDefinitionBuilder.version(version),
            conveyorPluginSourceCodeBuilder.version(version),
            moduleInfoSourceCodeBuilder
        );
    }

    ConveyorPluginBuilder stage(Stage stage) {
        return new ConveyorPluginBuilder(
            projectDefinitionBuilder,
            conveyorPluginSourceCodeBuilder.stage(stage),
            moduleInfoSourceCodeBuilder
        );
    }

    ConveyorPluginBuilder dependency(ProjectDefinitionBuilder builder, DependencyScope scope) {
        return new ConveyorPluginBuilder(
            projectDefinitionBuilder.dependency(builder, scope),
            conveyorPluginSourceCodeBuilder,
            moduleInfoSourceCodeBuilder
        );
    }

    ConveyorPluginBuilder dependency(DependencyBuilder builder) {
        return new ConveyorPluginBuilder(
            projectDefinitionBuilder.dependency(builder, DependencyScope.IMPLEMENTATION),
            conveyorPluginSourceCodeBuilder.dependency(builder.fullyQualifiedName()),
            moduleInfoSourceCodeBuilder.requires(builder.module())
        );
    }
}

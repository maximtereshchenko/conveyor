package com.github.maximtereshchenko.conveyor.domain;

import com.github.maximtereshchenko.conveyor.common.api.DependencyScope;

import java.util.Set;

abstract class ArtifactDependency extends StoredArtifact<ArtifactDependencyModel>
    implements Dependency {

    private final ArtifactDependencyModel artifactDependencyModel;
    private final ModelFactory modelFactory;
    private final Properties properties;
    private final Preferences preferences;

    ArtifactDependency(
        ArtifactDependencyModel artifactDependencyModel,
        ModelFactory modelFactory,
        Properties properties,
        Preferences preferences,
        Repositories repositories
    ) {
        super(repositories);
        this.artifactDependencyModel = artifactDependencyModel;
        this.modelFactory = modelFactory;
        this.properties = properties;
        this.preferences = preferences;
    }

    @Override
    public String group() {
        return artifactDependencyModel.group();
    }

    @Override
    public String name() {
        return artifactDependencyModel.name();
    }

    @Override
    public SemanticVersion version() {
        return version(artifactDependencyModel, properties, preferences);
    }

    @Override
    public DependencyScope scope() {
        return artifactDependencyModel.scope().orElse(DependencyScope.IMPLEMENTATION);
    }

    @Override
    Set<ArtifactDependencyModel> dependencyModels() {
        return modelFactory.manualHierarchy(
                artifactDependencyModel.group(),
                artifactDependencyModel.name(),
                version(),
                repositories()
            )
            .dependencies();
    }

    @Override
    Dependency dependency(ArtifactDependencyModel dependencyModel) {
        return new TransitiveDependency(
            dependencyModel,
            modelFactory,
            properties,
            preferences,
            repositories()
        );
    }

    abstract SemanticVersion version(
        ArtifactDependencyModel artifactDependencyModel,
        Properties properties,
        Preferences preferences
    );
}

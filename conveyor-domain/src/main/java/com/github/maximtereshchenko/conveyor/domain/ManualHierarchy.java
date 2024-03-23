package com.github.maximtereshchenko.conveyor.domain;

import java.util.LinkedHashSet;
import java.util.Set;

final class ManualHierarchy extends
    Hierarchy<NoTemplateModel,
        ArtifactDependencyModel,
        Model<ManualTemplateModel, ArtifactDependencyModel>> {

    private ManualHierarchy(LinkedHashSet<Model<ManualTemplateModel, ArtifactDependencyModel>> models) {
        super(models);
    }

    ManualHierarchy(StandaloneManualModel standaloneManualModel) {
        this(new LinkedHashSet<>(Set.of(standaloneManualModel)));
    }

    @Override
    public NoTemplateModel template() {
        return new NoTemplateModel();
    }

    @Override
    public Set<ArtifactDependencyModel> dependencies() {
        return reduce(
            Model::dependencies,
            ArtifactDependencyModel::name,
            ArtifactDependencyModel::override
        );
    }

    ManualHierarchy inheritedFrom(StandaloneManualModel standaloneManualModel) {
        var copy = new LinkedHashSet<>(models());
        copy.addFirst(standaloneManualModel);
        return new ManualHierarchy(copy);
    }
}

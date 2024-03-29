package com.github.maximtereshchenko.conveyor.domain;

import com.github.maximtereshchenko.conveyor.api.port.DefinitionTranslator;

import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final class ModelFactory {

    private final DefinitionTranslator definitionTranslator;

    ModelFactory(DefinitionTranslator definitionTranslator) {
        this.definitionTranslator = definitionTranslator;
    }

    LinkedHashSet<PartialSchematicHierarchy> partialSchematicHierarchies(Path path) {
        return allSchematicPaths(root(path))
            .map(this::partialSchematicHierarchy)
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    ManualHierarchy manualHierarchy(
        String group,
        String name,
        SemanticVersion version,
        Repositories repositories
    ) {
        return manualHierarchy(group, name, version, repositories, ManualHierarchy::new);
    }

    FullSchematicHierarchy fullSchematicHierarchy(
        PartialSchematicHierarchy partialSchematicHierarchy,
        Repositories repositories
    ) {
        return switch (partialSchematicHierarchy.template()) {
            case NoTemplateModel ignored -> new FullSchematicHierarchy(partialSchematicHierarchy);
            case OtherManualTemplateModel model -> new FullSchematicHierarchy(
                manualHierarchy(model.group(), model.name(), model.version(), repositories),
                partialSchematicHierarchy
            );
            case OtherSchematicTemplateModel ignored -> throw new IllegalArgumentException();
        };
    }

    FullSchematicHierarchy fullSchematicHierarchy(Path path, Repositories repositories) {
        return fullSchematicHierarchy(partialSchematicHierarchy(path), repositories);
    }

    private ManualHierarchy manualHierarchy(
        String group,
        String name,
        SemanticVersion version,
        Repositories repositories,
        Function<StandaloneManualModel, ManualHierarchy> combiner
    ) {
        var standaloneManualModel = new StandaloneManualModel(
            repositories.manualDefinition(
                group,
                name,
                version
            )
        );
        return manualHierarchy(
            standaloneManualModel.template(),
            repositories,
            combiner.apply(standaloneManualModel)
        );
    }

    private ManualHierarchy manualHierarchy(
        ManualTemplateModel manualTemplateModel,
        Repositories repositories,
        ManualHierarchy manualHierarchy
    ) {
        return switch (manualTemplateModel) {
            case NoTemplateModel ignored -> manualHierarchy;
            case OtherManualTemplateModel model -> manualHierarchy(
                model.group(),
                model.name(),
                model.version(),
                repositories,
                manualHierarchy::inheritedFrom
            );
        };
    }

    private Stream<Path> allSchematicPaths(Path path) {
        return Stream.concat(
            Stream.of(path),
            standaloneSchematicModel(path)
                .inclusions()
                .stream()
                .flatMap(this::allSchematicPaths)
        );
    }

    private Path root(Path path) {
        return switch (standaloneSchematicModel(path).template()) {
            case OtherManualTemplateModel ignored -> path;
            case NoTemplateModel ignored -> path;
            case OtherSchematicTemplateModel model -> root(model.path());
        };
    }

    private StandaloneSchematicModel standaloneSchematicModel(Path path) {
        return new StandaloneSchematicModel(definitionTranslator.schematicDefinition(path), path);
    }

    private PartialSchematicHierarchy partialSchematicHierarchy(Path path) {
        return partialSchematicHierarchy(path, PartialSchematicHierarchy::new);
    }

    private PartialSchematicHierarchy partialSchematicHierarchy(
        Path path,
        Function<StandaloneSchematicModel, PartialSchematicHierarchy> combiner
    ) {
        var standaloneSchematicModel = standaloneSchematicModel(path);
        return partialSchematicHierarchy(
            standaloneSchematicModel.template(),
            combiner.apply(standaloneSchematicModel)
        );
    }

    private PartialSchematicHierarchy partialSchematicHierarchy(
        SchematicTemplateModel schematicTemplateModel,
        PartialSchematicHierarchy partialSchematicHierarchy
    ) {
        return switch (schematicTemplateModel) {
            case OtherManualTemplateModel ignored -> partialSchematicHierarchy;
            case NoTemplateModel ignored -> partialSchematicHierarchy;
            case OtherSchematicTemplateModel model ->
                partialSchematicHierarchy(model.path(), partialSchematicHierarchy::inheritedFrom);
        };
    }
}

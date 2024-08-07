package com.github.maximtereshchenko.conveyor.core;

import com.github.maximtereshchenko.conveyor.api.port.SchematicDefinitionConverter;
import com.github.maximtereshchenko.conveyor.api.schematic.*;
import com.github.maximtereshchenko.conveyor.files.FileTree;

import java.io.ByteArrayInputStream;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;

final class MavenRepositoryAdapter implements Repository<Path, Resource> {

    private final Repository<Path, Path> original;
    private final PomDefinitionFactory pomDefinitionFactory;
    private final SchematicDefinitionConverter schematicDefinitionConverter;

    MavenRepositoryAdapter(
        Repository<Path, Path> original,
        PomDefinitionFactory pomDefinitionFactory,
        SchematicDefinitionConverter schematicDefinitionConverter
    ) {
        this.original = original;
        this.pomDefinitionFactory = pomDefinitionFactory;
        this.schematicDefinitionConverter = schematicDefinitionConverter;
    }

    @Override
    public void publish(
        Id id,
        Version version,
        Classifier classifier,
        Path artifact
    ) {
        original.publish(id, version, classifier, artifact);
    }

    @Override
    public Optional<Resource> artifact(
        Id id,
        Version version,
        Classifier classifier
    ) {
        return switch (classifier) {
            case SCHEMATIC_DEFINITION -> original.artifact(id, version, Classifier.POM)
                .map(this::pomDefinition)
                .map(this::schematicDefinition)
                .map(this::resource);
            case CLASSES, POM -> original.artifact(id, version, classifier)
                .map(Resource::new);
        };
    }

    private Resource resource(SchematicDefinition schematicDefinition) {
        return new Resource(() ->
            new ByteArrayInputStream(schematicDefinitionConverter.bytes(schematicDefinition))
        );
    }

    private PomDefinition pomDefinition(Path path) {
        return new FileTree(path).read(pomDefinitionFactory::pomDefinition);
    }

    private SchematicDefinition schematicDefinition(PomDefinition pomDefinition) {
        return new SchematicDefinition(
            pomDefinition.groupId(),
            pomDefinition.artifactId(),
            pomDefinition.version(),
            template(pomDefinition),
            List.of(),
            List.of(),
            properties(
                pomDefinition.properties(),
                either(pomDefinition, PomDefinition::groupId, PomDefinition.Parent::groupId),
                either(pomDefinition, PomDefinition::version, PomDefinition.Parent::version)
            ),
            preferences(pomDefinition),
            List.of(),
            dependencies(pomDefinition, scopes(pomDefinition))
        );
    }

    private Map<String, String> properties(
        Map<String, String> original,
        String groupId,
        String version
    ) {
        var properties = new HashMap<>(original);
        properties.put("project.groupId", groupId);
        properties.put("project.version", version);
        return properties;
    }

    private Map<Id, PomDefinition.DependencyScope> scopes(PomDefinition pomModel) {
        var scopes = pomModel.parent()
            .flatMap(parent ->
                original.artifact(
                    new Id(parent.groupId(), parent.artifactId()),
                    new Version(parent.version()),
                    Classifier.POM
                )
            )
            .map(this::pomDefinition)
            .map(this::scopes)
            .orElseGet(HashMap::new);
        for (var definition : pomModel.dependencyManagement()) {
            definition.scope()
                .ifPresent(scope ->
                    scopes.put(new Id(definition.groupId(), definition.artifactId()), scope)
                );
        }
        return scopes;
    }

    private List<DependencyDefinition> dependencies(
        PomDefinition pomModel,
        Map<Id, PomDefinition.DependencyScope> scopes
    ) {
        return pomModel.dependencies()
            .stream()
            .filter(definition -> !definition.optional())
            .map(definition ->
                new DependencyDefinition(
                    definition.groupId(),
                    definition.artifactId(),
                    definition.version(),
                    scope(scopes, definition),
                    exclusions(definition)
                )
            )
            .toList();
    }

    private List<ExclusionDefinition> exclusions(PomDefinition.DependencyDefinition definition) {
        return definition.exclusions()
            .stream()
            .map(exclusion ->
                new ExclusionDefinition(exclusion.groupId(), exclusion.artifactId())
            )
            .toList();
    }

    private Optional<DependencyScope> scope(
        Map<Id, PomDefinition.DependencyScope> scopes,
        PomDefinition.DependencyDefinition definition
    ) {
        return definition.scope()
            .or(() ->
                Optional.ofNullable(
                    scopes.get(new Id(definition.groupId(), definition.artifactId()))
                )
            )
            .map(scope ->
                switch (scope) {
                    case COMPILE, RUNTIME, SYSTEM, PROVIDED -> DependencyScope.IMPLEMENTATION;
                    case TEST -> DependencyScope.TEST;
                    case IMPORT -> throw new IllegalArgumentException();
                }
            );
    }

    private PreferencesDefinition preferences(PomDefinition pomModel) {
        var inclusions = new ArrayList<PreferencesInclusionDefinition>();
        var artifacts = new ArrayList<ArtifactPreferenceDefinition>();
        for (var definition : pomModel.dependencyManagement()) {
            if (isImportScoped(definition)) {
                inclusions.add(
                    new PreferencesInclusionDefinition(
                        definition.groupId(),
                        definition.artifactId(),
                        definition.version()
                    )
                );
            } else {
                artifacts.add(
                    new ArtifactPreferenceDefinition(
                        definition.groupId(),
                        definition.artifactId(),
                        definition.version()
                    )
                );
            }
        }
        return new PreferencesDefinition(inclusions, artifacts);
    }

    private boolean isImportScoped(PomDefinition.ManagedDependencyDefinition definition) {
        return definition.scope()
            .map(PomDefinition.DependencyScope.IMPORT::equals)
            .orElse(Boolean.FALSE);
    }

    private TemplateDefinition template(PomDefinition pomModel) {
        return pomModel.parent()
            .<TemplateDefinition>map(parent ->
                new SchematicTemplateDefinition(
                    parent.groupId(),
                    parent.artifactId(),
                    parent.version()
                )
            )
            .orElseGet(NoTemplateDefinition::new);
    }

    private String either(
        PomDefinition pomModel,
        Function<PomDefinition, Optional<String>> pomFunction,
        Function<PomDefinition.Parent, String> parentFunction
    ) {
        return pomFunction.apply(pomModel)
            .or(() -> pomModel.parent().map(parentFunction))
            .orElseThrow();
    }
}

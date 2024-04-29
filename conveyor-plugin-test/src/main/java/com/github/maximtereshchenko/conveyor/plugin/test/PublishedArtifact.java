package com.github.maximtereshchenko.conveyor.plugin.test;

import com.github.maximtereshchenko.conveyor.plugin.api.ArtifactClassifier;

import java.nio.file.Path;

public record PublishedArtifact(
    String repository,
    Path path,
    ArtifactClassifier artifactClassifier
) {}

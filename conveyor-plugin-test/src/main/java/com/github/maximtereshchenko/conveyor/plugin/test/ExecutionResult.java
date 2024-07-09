package com.github.maximtereshchenko.conveyor.plugin.test;

public sealed interface ExecutionResult permits Failure, Success {

    void thenNoException();

    void thenNoArtifactPublished();

    void thenArtifactsPublished(PublishedArtifact... artifacts);
}

package com.github.maximtereshchenko.conveyor.plugin.test;

import static com.github.maximtereshchenko.conveyor.common.test.MoreAssertions.assertThat;

final class Failure implements ExecutionResult {

    private final Exception e;

    Failure(Exception e) {
        this.e = e;
    }

    @Override
    public void thenNoException() {
        assertThat(e).doesNotThrowAnyException();
    }

    @Override
    public void thenNoArtifactPublished() {
        thenNoException();
    }

    @Override
    public void thenArtifactsPublished(PublishedArtifact... artifacts) {
        thenNoException();
    }
}

package com.github.maximtereshchenko.conveyor.plugin.test;

import static com.github.maximtereshchenko.conveyor.common.test.MoreAssertions.assertThat;

final class Success implements ExecutionResult {

    private final FakeConveyorSchematic schematic;

    Success(FakeConveyorSchematic schematic) {
        this.schematic = schematic;
    }

    @Override
    public void thenNoException() {
        //empty
    }

    @Override
    public void thenNoArtifactPublished() {
        assertThat(schematic.published()).isEmpty();
    }

    @Override
    public void thenArtifactsPublished(PublishedArtifact... artifacts) {
        assertThat(schematic.published()).containsExactly(artifacts);
    }
}

package com.github.maximtereshchenko.conveyor.common.test;

import org.assertj.core.api.Assertions;

import java.nio.file.Path;

public final class MoreAssertions extends Assertions {

    public static DirectoryAssert assertThat(Path actual) {
        return new DirectoryAssert(actual);
    }
}

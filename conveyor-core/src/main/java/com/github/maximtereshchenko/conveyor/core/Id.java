package com.github.maximtereshchenko.conveyor.core;

import java.nio.file.Path;
import java.util.stream.Stream;

record Id(String group, String name) {

    @Override
    public String toString() {
        return group + ':' + name;
    }

    Path path(Path base) {
        return Stream.concat(
                Stream.of(group.split("\\.")),
                Stream.of(name)
            )
            .reduce(base, Path::resolve, (a, b) -> a);
    }
}

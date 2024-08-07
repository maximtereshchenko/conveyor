package com.github.maximtereshchenko.conveyor.core;

import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorTaskInput;
import com.github.maximtereshchenko.conveyor.plugin.api.KeyValueConveyorTaskInput;
import com.github.maximtereshchenko.conveyor.plugin.api.PathConveyorTaskInput;

import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.zip.Checksum;

final class Inputs extends Boundaries<ConveyorTaskInput> {

    private final Set<ConveyorTaskInput> all;
    private boolean isChecksumCached = false;
    private long checksum = 0;

    Inputs(Set<ConveyorTaskInput> all) {
        this.all = all;
    }

    @Override
    long checksum() {
        if (isChecksumCached) {
            return checksum;
        }
        checksum = checksum(all);
        isChecksumCached = true;
        return checksum;
    }

    @Override
    void update(Checksum checksum, ConveyorTaskInput element) {
        switch (element) {
            case PathConveyorTaskInput pathBoundary -> update(checksum, pathBoundary.path());
            case KeyValueConveyorTaskInput keyValueInput -> {
                update(checksum, keyValueInput.key());
                update(checksum, keyValueInput.value());
            }
        }
    }

    private void update(Checksum checksum, String string) {
        checksum.update(string.getBytes(StandardCharsets.UTF_8));
    }
}

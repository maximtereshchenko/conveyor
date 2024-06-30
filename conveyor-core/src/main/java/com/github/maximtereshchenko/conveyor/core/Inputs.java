package com.github.maximtereshchenko.conveyor.core;

import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorTaskInput;
import com.github.maximtereshchenko.conveyor.plugin.api.PathConveyorTaskInput;

import java.util.Set;
import java.util.zip.Checksum;

final class Inputs extends Boundaries<ConveyorTaskInput> {

    Inputs(Set<ConveyorTaskInput> all) {
        super(all);
    }

    @Override
    void update(Checksum checksum, ConveyorTaskInput element) {
        switch (element) {
            case PathConveyorTaskInput pathBoundary -> update(checksum, pathBoundary.path());
        }
    }
}

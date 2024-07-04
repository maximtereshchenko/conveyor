package com.github.maximtereshchenko.conveyor.core;

import com.github.maximtereshchenko.conveyor.filevisitors.Delete;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorTaskOutput;
import com.github.maximtereshchenko.conveyor.plugin.api.PathConveyorTaskOutput;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.Checksum;

final class Outputs extends Boundaries<ConveyorTaskOutput> {

    private final Set<ConveyorTaskOutput> all;

    Outputs(Set<ConveyorTaskOutput> all) {
        this.all = all;
    }

    @Override
    long checksum() {
        return checksum(all);
    }

    @Override
    void update(Checksum checksum, ConveyorTaskOutput element) {
        switch (element) {
            case PathConveyorTaskOutput pathOutput -> update(checksum, pathOutput.path());
        }
    }

    Set<Path> paths() {
        return all.stream()
            .map(output ->
                switch (output) {
                    case PathConveyorTaskOutput pathOutput -> pathOutput.path();
                }
            )
            .collect(Collectors.toSet());
    }

    void delete() {
        for (var output : all) {
            switch (output) {
                case PathConveyorTaskOutput pathOutput -> {
                    var path = pathOutput.path();
                    if (Files.isDirectory(path)) {
                        try {
                            Files.walkFileTree(path, new Delete());
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    }
                }
            }
        }
    }
}

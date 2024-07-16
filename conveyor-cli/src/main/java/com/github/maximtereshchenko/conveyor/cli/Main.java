package com.github.maximtereshchenko.conveyor.cli;

import com.github.maximtereshchenko.conveyor.api.Stage;
import com.github.maximtereshchenko.conveyor.api.TracingOutputLevel;
import com.github.maximtereshchenko.conveyor.core.ConveyorFacade;
import com.github.maximtereshchenko.conveyor.jackson.JacksonAdapter;

import java.nio.file.Paths;
import java.util.Arrays;

final class Main {

    public static void main(String[] args) {
        new ConveyorFacade(
            JacksonAdapter.configured(),
            System.out::println,
            TracingOutputLevel.VERBOSE
        )
            .construct(
                Paths.get(args[0]).toAbsolutePath().normalize(),
                Arrays.stream(args, 1, args.length)
                    .map(String::toUpperCase)
                    .map(Stage::valueOf)
                    .toList()
            );
        System.exit(0); //TODO run tests in separate JVM
    }
}

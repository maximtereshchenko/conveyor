package com.github.maximtereshchenko.conveyor.cli;

import picocli.CommandLine;

final class Main {

    public static void main(String[] args) {
        System.exit(
            new CommandLine(new ConveyorCommand())
                .setCaseInsensitiveEnumValuesAllowed(true)
                .execute(args)
        );
    }
}

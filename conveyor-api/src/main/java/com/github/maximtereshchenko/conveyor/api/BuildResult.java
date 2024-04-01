package com.github.maximtereshchenko.conveyor.api;

public sealed interface BuildResult permits BuildSucceeded, CouldNotFindProjectDefinition {}

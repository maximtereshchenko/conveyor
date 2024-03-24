package com.github.maximtereshchenko.conveyor.domain;

enum ConveyorPropertyKey {

    SCHEMATIC_NAME("conveyor.schematic.name"),
    SCHEMATIC_VERSION("conveyor.schematic.version"),
    DISCOVERY_DIRECTORY("conveyor.discovery.directory"),
    CONSTRUCTION_DIRECTORY("conveyor.construction.directory"),
    REMOTE_REPOSITORY_CACHE_DIRECTORY("conveyor.repository.remote.cache.directory");

    private final String fullName;

    ConveyorPropertyKey(String fullName) {
        this.fullName = fullName;
    }

    String fullName() {
        return fullName;
    }
}

package com.github.maximtereshchenko.conveyor.domain;

enum SchematicPropertyKey {

    NAME("conveyor.schematic.name"),
    VERSION("conveyor.schematic.version"),
    DISCOVERY_DIRECTORY("conveyor.discovery.directory"),
    CONSTRUCTION_DIRECTORY("conveyor.construction.directory");

    private final String fullName;

    SchematicPropertyKey(String fullName) {
        this.fullName = fullName;
    }

    String fullName() {
        return fullName;
    }
}

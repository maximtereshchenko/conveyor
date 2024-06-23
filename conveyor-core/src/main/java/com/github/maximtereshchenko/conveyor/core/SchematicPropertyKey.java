package com.github.maximtereshchenko.conveyor.core;

enum SchematicPropertyKey {

    TEMPLATE_LOCATION("conveyor.schematic.template.location"),
    SCHEMATIC_GROUP("conveyor.schematic.group"),
    SCHEMATIC_NAME("conveyor.schematic.name"),
    SCHEMATIC_VERSION("conveyor.schematic.version"),
    SCHEMATIC_DIRECTORY("conveyor.schematic.directory"),
    REMOTE_REPOSITORY_CACHE_DIRECTORY("conveyor.repository.remote.cache.directory");

    private final String fullName;

    SchematicPropertyKey(String fullName) {
        this.fullName = fullName;
    }

    String fullName() {
        return fullName;
    }
}

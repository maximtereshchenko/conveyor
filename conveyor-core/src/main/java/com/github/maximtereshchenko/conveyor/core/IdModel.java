package com.github.maximtereshchenko.conveyor.core;

record IdModel(String group, String name) {

    Id id(Properties properties) {
        return new Id(properties.interpolated(group), name);
    }
}

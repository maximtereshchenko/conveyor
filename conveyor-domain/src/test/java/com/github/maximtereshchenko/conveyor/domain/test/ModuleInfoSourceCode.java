package com.github.maximtereshchenko.conveyor.domain.test;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

final class ModuleInfoSourceCode {

    private final String name;
    private final Collection<String> requires;
    private final Map<String, String> provides;
    private final Collection<String> exports;

    ModuleInfoSourceCode(
        String name,
        Collection<String> requires,
        Map<String, String> provides,
        Collection<String> exports
    ) {
        this.name = name;
        this.requires = requires;
        this.provides = provides;
        this.exports = exports;
    }

    @Override
    public String toString() {
        return """
            module %s {
                %s
                %s
                %s
            }
            """
            .formatted(
                name,
                requires.stream()
                    .map("requires %s;"::formatted)
                    .collect(Collectors.joining()),
                provides.entrySet()
                    .stream()
                    .map(entry -> "provides %s with %s;".formatted(entry.getKey(), entry.getValue()))
                    .collect(Collectors.joining()),
                exports.stream()
                    .map("exports %s;"::formatted)
                    .collect(Collectors.joining())
            );
    }
}

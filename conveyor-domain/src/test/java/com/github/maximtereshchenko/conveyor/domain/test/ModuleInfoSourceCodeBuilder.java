package com.github.maximtereshchenko.conveyor.domain.test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

final class ModuleInfoSourceCodeBuilder {

    private final String name;
    private final Collection<String> requires;
    private final String conveyorPluginName;
    private final Collection<String> exports;

    private ModuleInfoSourceCodeBuilder(
        String name,
        Collection<String> requires,
        String conveyorPluginName,
        Collection<String> exports
    ) {
        this.name = name;
        this.requires = List.copyOf(requires);
        this.conveyorPluginName = conveyorPluginName;
        this.exports = List.copyOf(exports);
    }

    ModuleInfoSourceCodeBuilder(String name) {
        this(name, List.of(), "", List.of());
    }

    ModuleInfoSourceCodeBuilder name(String name) {
        return new ModuleInfoSourceCodeBuilder(name, requires, conveyorPluginName, exports);
    }

    ModuleInfoSourceCodeBuilder requires(String module) {
        var copy = new ArrayList<>(requires);
        copy.add(module);
        return new ModuleInfoSourceCodeBuilder(name, copy, conveyorPluginName, exports);
    }

    ModuleInfoSourceCodeBuilder providesConveyorPlugin(String conveyorPluginName) {
        return new ModuleInfoSourceCodeBuilder(name, requires, conveyorPluginName, exports);
    }

    String build() {
        return """
            module %s {
                %s
                %s
                %s
            }
            """
            .formatted(
                normalizedName(),
                requires.stream()
                    .map("requires %s;"::formatted)
                    .collect(Collectors.joining()),
                provides(),
                "exports %s;".formatted(normalizedName())
            );
    }

    private String provides() {
        if (conveyorPluginName.isBlank()) {
            return "";
        }
        return "provides com.github.maximtereshchenko.conveyor.plugin.api.ConveyorPlugin with %s;"
            .formatted(conveyorPluginName);
    }

    private String normalizedName() {
        return name.toLowerCase(Locale.ROOT).replace("-", "");
    }
}

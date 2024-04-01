package com.github.maximtereshchenko.conveyor.api.schematic;

public final class NoTemplateDefinition implements TemplateDefinition {

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public boolean equals(Object object) {
        return this == object || (object != null && getClass() == object.getClass());
    }
}

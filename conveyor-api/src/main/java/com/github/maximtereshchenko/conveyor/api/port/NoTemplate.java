package com.github.maximtereshchenko.conveyor.api.port;

import java.util.Objects;

public final class NoTemplate
    implements TemplateForSchematicDefinition, TemplateForManualDefinition {

    @Override
    public int hashCode() {
        return Objects.hash();
    }

    @Override
    public boolean equals(Object object) {
        return this == object || (object != null && getClass() == object.getClass());
    }
}

package com.github.maximtereshchenko.conveyor.domain;

import java.nio.file.Path;

final class OverriddenPlugin implements Plugin {

    private final Plugin original;
    private final Plugin override;

    OverriddenPlugin(Plugin original, Plugin override) {
        this.original = original;
        this.override = override;
    }

    @Override
    public String name() {
        return override.name();
    }

    @Override
    public int version() {
        return override.version();
    }

    @Override
    public Dependencies dependencies() {
        return override.dependencies();
    }

    @Override
    public Path modulePath() {
        return override.modulePath();
    }

    @Override
    public Configuration configuration() {
        return original.configuration().override(override.configuration());
    }
}

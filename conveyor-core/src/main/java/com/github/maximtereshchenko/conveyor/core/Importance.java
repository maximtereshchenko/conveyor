package com.github.maximtereshchenko.conveyor.core;

import com.github.maximtereshchenko.conveyor.api.TracingLevel;

enum Importance {

    DEBUG(TracingLevel.VERBOSE),
    INFO(TracingLevel.NORMAL),
    WARN(TracingLevel.SILENT);

    private final TracingLevel minimalOutputLevel;

    Importance(TracingLevel minimalOutputLevel) {
        this.minimalOutputLevel = minimalOutputLevel;
    }

    TracingLevel minimalOutputLevel() {
        return minimalOutputLevel;
    }
}

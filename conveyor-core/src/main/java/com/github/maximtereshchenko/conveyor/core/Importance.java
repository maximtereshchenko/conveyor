package com.github.maximtereshchenko.conveyor.core;

import com.github.maximtereshchenko.conveyor.api.TracingOutputLevel;

enum Importance {

    DEBUG(TracingOutputLevel.VERBOSE),
    INFO(TracingOutputLevel.NORMAL),
    WARN(TracingOutputLevel.SILENT);

    private final TracingOutputLevel minimalOutputLevel;

    Importance(TracingOutputLevel minimalOutputLevel) {
        this.minimalOutputLevel = minimalOutputLevel;
    }

    TracingOutputLevel minimalOutputLevel() {
        return minimalOutputLevel;
    }
}

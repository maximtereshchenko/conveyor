package com.github.maximtereshchenko.conveyor.domain;

import com.github.maximtereshchenko.conveyor.api.SchematicProducts;

final class EmptyManual implements Manual {

    @Override
    public Properties properties() {
        return new Properties();
    }

    @Override
    public Plugins plugins() {
        return new Plugins();
    }

    @Override
    public Dependencies dependencies(SchematicProducts schematicProducts) {
        return new Dependencies();
    }
}

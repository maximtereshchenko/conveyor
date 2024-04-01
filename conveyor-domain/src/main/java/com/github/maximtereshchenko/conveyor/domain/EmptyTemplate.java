package com.github.maximtereshchenko.conveyor.domain;

import com.github.maximtereshchenko.conveyor.api.SchematicProducts;

import java.util.Optional;

final class EmptyTemplate implements Template {

    @Override
    public Optional<Repository> repository() {
        return Optional.empty();
    }

    @Override
    public Properties properties(Repository repository) {
        return new Properties();
    }

    @Override
    public Plugins plugins(Repository repository) {
        return new Plugins();
    }

    @Override
    public Dependencies dependencies(Repository repository, SchematicProducts schematicProducts) {
        return new Dependencies();
    }
}

package com.github.maximtereshchenko.conveyor.domain;

import com.github.maximtereshchenko.conveyor.api.SchematicProducts;

import java.util.Optional;

final class EmptyTemplate implements Template {

    @Override
    public Repositories repositories() {
        return new Repositories();
    }

    @Override
    public Properties properties(Repositories repositories) {
        return new Properties();
    }

    @Override
    public Plugins plugins(Repositories repositories) {
        return new Plugins();
    }

    @Override
    public Dependencies dependencies(Repositories repositories, SchematicProducts schematicProducts) {
        return new Dependencies();
    }

    @Override
    public Optional<Schematic> root() {
        return Optional.empty();
    }

    @Override
    public boolean inheritsFrom(Schematic schematic) {
        return false;
    }
}

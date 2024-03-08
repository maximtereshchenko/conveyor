package com.github.maximtereshchenko.conveyor.domain;

final class PluginWithoutVersion implements Plugin {

    private final String name;
    private final Configuration configuration;

    PluginWithoutVersion(String name, Configuration configuration) {
        this.name = name;
        this.configuration = configuration;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public int version() {
        throw new IllegalStateException();
    }

    @Override
    public boolean isEnabled() {
        return configuration().isEnabled();
    }

    @Override
    public Configuration configuration() {
        return configuration;
    }

    @Override
    public Plugin override(Plugin base) {
        return new VersionedPlugin(name, base.version(), configuration.override(base.configuration()));
    }

    @Override
    public Artifact artifact(Repositories repositories) {
        throw new IllegalStateException();
    }
}

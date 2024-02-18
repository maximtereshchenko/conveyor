package com.github.maximtereshchenko.conveyor.domain;

final class VersionedPlugin implements Plugin {

    private final String name;
    private final int version;
    private final Configuration configuration;

    VersionedPlugin(String name, int version, Configuration configuration) {
        this.name = name;
        this.version = version;
        this.configuration = configuration;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public int version() {
        return version;
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
        return new VersionedPlugin(name, version, configuration.override(base.configuration()));
    }

    @Override
    public Artifact artifact(Repository repository) {
        return new PackagedArtifact(name, version, repository);
    }
}

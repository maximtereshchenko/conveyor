package com.github.maximtereshchenko.conveyor.domain;

interface Plugin {

    String name();

    int version();

    boolean isEnabled();

    Configuration configuration();

    Plugin override(Plugin base);

    Artifact artifact(Repositories repositories);
}

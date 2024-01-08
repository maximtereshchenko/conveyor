package com.github.maximtereshchenko.conveyor.plugin.dependency;

import java.nio.file.Paths;

public final class Dependency {

    public String version() {
        var name = Paths.get(
                getClass().getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .getFile()
            )
            .getFileName()
            .toString();
        return name.substring(name.length() - 5, name.length() - 4);
    }
}

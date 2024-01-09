package com.github.maximtereshchenko.conveyor.plugin.dependency;

import com.github.maximtereshchenko.conveyor.plugin.transitivedependency.TransitiveDependency;
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
        return "%c-%s".formatted(name.charAt(name.length() - 5), new TransitiveDependency().suffix());
    }
}

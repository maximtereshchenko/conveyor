package com.github.maximtereshchenko.conveyor.api;

import com.github.maximtereshchenko.conveyor.common.api.BuildFileType;
import com.github.maximtereshchenko.conveyor.common.api.BuildFiles;

import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public final class ProjectBuildFiles {

    private final Map<String, BuildFiles> files;

    private ProjectBuildFiles(Map<String, BuildFiles> files) {
        this.files = Map.copyOf(files);
    }

    public ProjectBuildFiles() {
        this(Map.of());
    }

    public ProjectBuildFiles with(String project, BuildFiles buildFiles) {
        var copy = new HashMap<>(files);
        copy.put(project, buildFiles);
        return new ProjectBuildFiles(copy);
    }

    public Collection<Path> byType(String project, BuildFileType buildFileType) {
        return files.get(project).byType(buildFileType);
    }
}

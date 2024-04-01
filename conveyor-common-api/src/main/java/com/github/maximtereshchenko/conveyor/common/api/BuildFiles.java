package com.github.maximtereshchenko.conveyor.common.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public final class BuildFiles {

    private final Collection<BuildFile> files;

    private BuildFiles(Collection<BuildFile> files) {
        this.files = List.copyOf(files);
    }

    public BuildFiles(BuildFile... files) {
        this(List.of(files));
    }

    public Collection<BuildFile> byType(BuildFileType type) {
        return files.stream()
            .filter(buildFile -> buildFile.type() == type)
            .toList();
    }

    public BuildFiles with(BuildFile file) {
        var copy = new ArrayList<>(files);
        copy.add(file);
        return new BuildFiles(copy);
    }

    public BuildFiles with(BuildFiles buildFiles) {
        var copy = new ArrayList<>(files);
        copy.addAll(buildFiles.files);
        return new BuildFiles(copy);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        var that = (BuildFiles) object;
        return Objects.equals(files, that.files);
    }

    @Override
    public int hashCode() {
        return Objects.hash(files);
    }

    @Override
    public String toString() {
        return files.toString();
    }
}

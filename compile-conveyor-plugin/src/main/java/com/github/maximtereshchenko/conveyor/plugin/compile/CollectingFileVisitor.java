package com.github.maximtereshchenko.conveyor.plugin.compile;

import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashSet;
import java.util.Set;

final class CollectingFileVisitor extends SimpleFileVisitor<Path> {

    private final Set<Path> collected = new HashSet<>();

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
        collected.add(file);
        return FileVisitResult.CONTINUE;
    }

    Set<Path> collected() {
        return collected;
    }
}

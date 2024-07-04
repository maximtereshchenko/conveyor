package com.github.maximtereshchenko.conveyor.files;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.TreeSet;

public final class FileTree {

    private final Path path;

    public FileTree(Path path) {
        this.path = path;
    }

    public void walk(FileVisitor<Path> visitor) {
        if (exists()) {
            execute(() -> Files.walkFileTree(path, visitor));
        }
    }

    public Set<Path> files() {
        var files = new TreeSet<Path>();
        walk(new Generic(files::add));
        return files;
    }

    public void copyTo(Path destination) {
        walk(new Copy(path, destination));
    }

    public void write(IOConsumer<OutputStream> consumer) {
        execute(() -> {
            Files.createDirectories(path.getParent());
            try (var outputStream = Files.newOutputStream(path)) {
                consumer.accept(outputStream);
            }
        });
    }

    public void write(long number) {
        write(String.valueOf(number));
    }

    public void write(String content) {
        write(outputStream -> outputStream.write(content.getBytes(StandardCharsets.UTF_8)));
    }

    public void transfer(IOConsumer<InputStream> consumer) {
        read(inputStream -> {
            consumer.accept(inputStream);
            return null;
        });
    }

    public <R> R read(IOFunction<InputStream, R> function) {
        return apply(() -> {
            try (var inputStream = Files.newInputStream(path)) {
                return function.apply(inputStream);
            }
        });
    }

    public String read() {
        return apply(() -> Files.readString(path));
    }

    public boolean exists() {
        return Files.exists(path);
    }

    public boolean isDirectory() {
        return Files.isDirectory(path);
    }

    public void delete() {
        execute(() -> {
            if (exists()) {
                walk(new Delete());
            }
        });
    }

    private void execute(IOAction action) {
        apply(() -> {
            action.execute();
            return null;
        });
    }

    private <R> R apply(IOSupplier<R> supplier) {
        try {
            return supplier.get();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}

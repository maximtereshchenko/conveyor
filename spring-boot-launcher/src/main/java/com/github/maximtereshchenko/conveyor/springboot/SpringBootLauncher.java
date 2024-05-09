package com.github.maximtereshchenko.conveyor.springboot;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.InvocationTargetException;
import java.net.*;
import java.nio.file.*;
import java.util.Map;

final class SpringBootLauncher {

    private final String classPathDirectory;
    private final String mainClassName;

    SpringBootLauncher(String classPathDirectory, String mainClassName) {
        this.classPathDirectory = classPathDirectory;
        this.mainClassName = mainClassName;
    }

    public void launch(String[] args)
        throws IOException,
        URISyntaxException,
        ClassNotFoundException,
        NoSuchMethodException,
        InvocationTargetException,
        IllegalAccessException {
        var directory = Files.createTempDirectory(null);
        copyClassPath(directory);
        var classLoader = URLClassLoader.newInstance(urls(directory));
        Thread.currentThread().setContextClassLoader(classLoader);
        invokeMain(classLoader, args);
    }

    private void invokeMain(ClassLoader classLoader, String[] args)
        throws ClassNotFoundException,
        NoSuchMethodException,
        InvocationTargetException,
        IllegalAccessException {
        classLoader.loadClass(mainClassName)
            .getDeclaredMethod("main", String[].class)
            .invoke(null, (Object) args);
    }

    private URL[] urls(Path directory) throws IOException {
        try (var files = Files.list(directory)) {
            return files.map(Path::toUri)
                .map(this::url)
                .toArray(URL[]::new);
        }
    }

    private URL url(URI uri) {
        try {
            return uri.toURL();
        } catch (MalformedURLException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void copyClassPath(Path directory) throws IOException, URISyntaxException {
        try (var fileSystem = fileSystem()) {
            var path = fileSystem.getPath(classPathDirectory);
            Files.walkFileTree(path, new CopyRecursively(path, directory));
        }
    }

    private FileSystem fileSystem() throws IOException, URISyntaxException {
        return FileSystems.newFileSystem(
            Paths.get(
                SpringBootLauncher.class
                    .getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .toURI()
            ),
            Map.of()
        );
    }
}

package com.github.maximtereshchenko.conveyor.core;

import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorPlugin;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorSchematic;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorTask;

import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.stream.Stream;

final class Plugins {

    private final LinkedHashSet<Plugin> all;
    private final ClasspathFactory classpathFactory;
    private final Tracer tracer;

    Plugins(LinkedHashSet<Plugin> all, ClasspathFactory classpathFactory, Tracer tracer) {
        this.all = all;
        this.classpathFactory = classpathFactory;
        this.tracer = tracer;
    }

    @Override
    public String toString() {
        return all.toString();
    }

    Tasks tasks(ConveyorSchematic conveyorSchematic, Properties properties) {
        var tasks = new Tasks(
            conveyorPlugins()
                .filter(conveyorPlugin -> named(conveyorPlugin.name()).isEnabled())
                .sorted(this::byPosition)
                .flatMap(conveyorPlugin -> tasks(conveyorSchematic, properties, conveyorPlugin))
                .sorted()
                .toList()
        );
        tracer.submitTasks(tasks);
        return tasks;
    }

    private Stream<Task> tasks(
        ConveyorSchematic conveyorSchematic,
        Properties properties,
        ConveyorPlugin conveyorPlugin
    ) {
        return conveyorPlugin.tasks(conveyorSchematic, named(conveyorPlugin.name()).configuration())
            .stream()
            .map(conveyorTask -> task(conveyorSchematic, properties, conveyorPlugin, conveyorTask));
    }

    private Task task(
        ConveyorSchematic conveyorSchematic,
        Properties properties,
        ConveyorPlugin conveyorPlugin,
        ConveyorTask conveyorTask
    ) {
        return new Task(
            conveyorTask,
            new TaskCache(properties.tasksCacheDirectory().resolve(conveyorTask.name())),
            conveyorSchematic.path().getParent(),
            tracer.withContext("plugin", conveyorPlugin.name())
                .withContext("task", conveyorTask.name())
        );
    }

    private int byPosition(ConveyorPlugin first, ConveyorPlugin second) {
        for (var plugin : all) {
            var name = plugin.id().name();
            if (name.equals(first.name())) {
                return -1;
            }
            if (name.equals(second.name())) {
                return 1;
            }
        }
        return 0;
    }

    private Stream<ConveyorPlugin> conveyorPlugins() {
        return ServiceLoader.load(
                ConveyorPlugin.class,
                classLoader(classpathFactory.classpath(all))
            )
            .stream()
            .map(ServiceLoader.Provider::get);
    }

    private Plugin named(String name) {
        return all.stream()
            .filter(plugin -> plugin.id().name().equals(name))
            .findAny()
            .orElseThrow();
    }

    private ClassLoader classLoader(Set<Path> paths) {
        return URLClassLoader.newInstance(
            paths.stream()
                .map(this::url)
                .toArray(URL[]::new),
            getClass().getClassLoader()
        );
    }

    private URL url(Path path) {
        try {
            return path.toUri().toURL();
        } catch (MalformedURLException e) {
            throw new UncheckedIOException(e);
        }
    }
}

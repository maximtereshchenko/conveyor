package com.github.maximtereshchenko.conveyor.core;

import com.github.maximtereshchenko.conveyor.common.api.Stage;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorPlugin;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorSchematic;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorTask;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorTaskBinding;

import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

final class Plugins {

    private static final System.Logger LOGGER = System.getLogger(Plugins.class.getName());

    private final LinkedHashSet<Plugin> all;
    private final ClasspathFactory classpathFactory;

    Plugins(LinkedHashSet<Plugin> all, ClasspathFactory classpathFactory) {
        this.all = all;
        this.classpathFactory = classpathFactory;
    }

    Optional<Path> executeTasks(ConveyorSchematic conveyorSchematic, Stage stage) {
        var artifacts = new ArrayList<Path>();
        for (var task : tasks(conveyorSchematic, stage)) {
            LOGGER.log(System.Logger.Level.INFO, "Executing task {0}", task.name());
            task.execute().ifPresent(artifacts::add);
        }
        return artifacts.isEmpty() ? Optional.empty() : Optional.of(artifacts.getLast());
    }

    private List<ConveyorTask> tasks(ConveyorSchematic conveyorSchematic, Stage stage) {
        return conveyorPlugins()
            .filter(conveyorPlugin -> named(conveyorPlugin.name()).isEnabled())
            .sorted(this::byPosition)
            .flatMap(conveyorPlugin -> bindings(conveyorSchematic, conveyorPlugin))
            .filter(binding -> isBeforeOrEqual(binding, stage))
            .sorted(byStageAndStep())
            .map(ConveyorTaskBinding::task)
            .toList();
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

    private Comparator<ConveyorTaskBinding> byStageAndStep() {
        return Comparator.comparing(ConveyorTaskBinding::stage).thenComparing(ConveyorTaskBinding::step);
    }

    private boolean isBeforeOrEqual(ConveyorTaskBinding conveyorTaskBinding, Stage stage) {
        return conveyorTaskBinding.stage().compareTo(stage) <= 0;
    }

    private Stream<ConveyorTaskBinding> bindings(
        ConveyorSchematic conveyorSchematic,
        ConveyorPlugin conveyorPlugin
    ) {
        return conveyorPlugin.bindings(
                conveyorSchematic,
                named(conveyorPlugin.name()).configuration()
            )
            .stream();
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

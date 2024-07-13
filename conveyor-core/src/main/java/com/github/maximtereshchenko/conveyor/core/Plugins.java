package com.github.maximtereshchenko.conveyor.core;

import com.github.maximtereshchenko.conveyor.api.Stage;
import com.github.maximtereshchenko.conveyor.plugin.api.BindingStage;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorPlugin;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorSchematic;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorTask;

import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final class Plugins {

    private static final System.Logger LOGGER = System.getLogger(Plugins.class.getName());
    private static final Map<BindingStage, BindingStage> STAGE_DEPENDENCIES = Map.of(
        BindingStage.PUBLISH, BindingStage.ARCHIVE,
        BindingStage.ARCHIVE, BindingStage.TEST,
        BindingStage.TEST, BindingStage.COMPILE
    );

    private final LinkedHashSet<Plugin> all;
    private final ClasspathFactory classpathFactory;

    Plugins(LinkedHashSet<Plugin> all, ClasspathFactory classpathFactory) {
        this.all = all;
        this.classpathFactory = classpathFactory;
    }

    void executeTasks(
        ConveyorSchematic conveyorSchematic,
        Properties properties,
        List<Stage> stages
    ) {
        var tasks = tasks(conveyorSchematic);
        for (var stage : stages) {
            var activeStages = activeStages(stage);
            for (var task : tasks) {
                if (!activeStages.contains(task.stage())) {
                    continue;
                }
                LOGGER.log(System.Logger.Level.INFO, "Executing task {0}", task.name());
                action(
                    task,
                    properties,
                    conveyorSchematic.path().getParent()
                )
                    .run();
            }
        }
    }

    private Set<BindingStage> activeStages(Stage stage) {
        return Stream.iterate(
                BindingStage.valueOf(stage.toString()),
                Objects::nonNull,
                STAGE_DEPENDENCIES::get
            )
            .collect(Collectors.toCollection(() -> EnumSet.noneOf(BindingStage.class)));
    }

    private Runnable action(
        ConveyorTask task,
        Properties properties,
        Path directory
    ) {
        return switch (task.cache()) {
            case ENABLED -> new CacheableAction(
                task.action(),
                new Inputs(task.inputs()),
                new Outputs(task.outputs()),
                new TaskCache(properties.tasksCacheDirectory().resolve(task.name())),
                directory
            );
            case DISABLED -> task.action();
        };
    }

    private List<ConveyorTask> tasks(ConveyorSchematic conveyorSchematic) {
        return conveyorPlugins()
            .filter(conveyorPlugin -> named(conveyorPlugin.name()).isEnabled())
            .sorted(this::byPosition)
            .map(conveyorPlugin ->
                conveyorPlugin.tasks(
                    conveyorSchematic,
                    named(conveyorPlugin.name()).configuration()
                )
            )
            .flatMap(Collection::stream)
            .sorted(byStageAndStep())
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

    private Comparator<ConveyorTask> byStageAndStep() {
        return Comparator.comparing(ConveyorTask::stage).thenComparing(ConveyorTask::step);
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

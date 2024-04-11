package ${normalizedName};

import com.github.maximtereshchenko.conveyor.common.api.*;
import com.github.maximtereshchenko.conveyor.plugin.api.*;

import java.io.*;
import java.lang.module.*;
import java.nio.file.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;

public final class ${normalizedName} implements ConveyorPlugin {

    @Override
    public String name() {
        return "${name}";
    }

    @Override
    public List<ConveyorTaskBinding> bindings(ConveyorSchematic schematic, Map<String, String> configuration) {
        return List.of(
            new ConveyorTaskBinding(
                Stage.COMPILE,
                Step.RUN,
                (products) -> execute(
                    schematic.modulePath(
                        Set.of(
                            DependencyScope.valueOf(
                                configuration.getOrDefault("scope", "IMPLEMENTATION")
                            )
                        )
                    ),
                    schematic.constructionDirectory().resolve("dependencies")
                )
            )
        );
    }

    private Set<Product> execute(Set<Path> paths, Path path) {
        write(path, modulePath(paths));
        return Set.of();
    }

    private String modulePath(Set<Path> paths) {
        return ServiceLoader.load(moduleLayer(paths), Supplier.class)
            .stream()
            .map(ServiceLoader.Provider::get)
            .map(Supplier::get)
            .map(Object::toString)
            .collect(Collectors.joining(System.lineSeparator()));
    }

    private ModuleLayer moduleLayer(Set<Path> paths) {
        var parent = ModuleLayer.boot();
        var moduleFinder = ModuleFinder.of(paths.toArray(Path[]::new));
        return parent.defineModulesWithOneLoader(
            parent.configuration()
                .resolveAndBind(
                    moduleFinder,
                    ModuleFinder.of(),
                    moduleFinder.findAll()
                        .stream()
                        .map(ModuleReference::descriptor)
                        .map(ModuleDescriptor::name)
                        .collect(Collectors.toSet())
                ),
            Thread.currentThread().getContextClassLoader()
        );
    }

    private void write(Path path, String content) {
        try {
            Files.createDirectories(path.getParent());
            Files.writeString(path, content);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}

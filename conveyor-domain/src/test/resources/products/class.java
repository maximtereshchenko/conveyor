package ${normalizedName};

import com.github.maximtereshchenko.conveyor.common.api.*;
import com.github.maximtereshchenko.conveyor.plugin.api.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;
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
                Stage.PUBLISH,
                Step.RUN,
                (conveyorSchematic, products) -> execute(products, schematic.constructionDirectory().resolve("products"))
            )
        );
    }

    private Set<Product> execute(Set<Product> products, Path path) {
        write(path, products(products));
        return Set.of();
    }

    private String products(Set<Product> products) {
        return products.stream()
            .map(product -> product.type() + "=" + product.path())
            .collect(Collectors.joining(System.lineSeparator()));
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

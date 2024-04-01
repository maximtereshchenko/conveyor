package ${normalizedName};

import com.github.maximtereshchenko.conveyor.common.api.*;
import com.github.maximtereshchenko.conveyor.plugin.api.*;

import java.io.*;
import java.nio.file.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;

public final class ${normalizedName} implements ConveyorPlugin {

    @Override
    public String name() {
        return "${name}";
    }

    @Override
    public List<ConveyorTaskBinding> bindings(ConveyorSchematic schematic, Map<String, String> configuration) {
        return configuration.entrySet()
            .stream()
            .filter(entry -> !entry.getKey().equals("enabled"))
            .map(entry -> binding(schematic.constructionDirectory().resolve(entry.getKey()), entry.getValue()))
            .toList();
    }

    private ConveyorTaskBinding binding(Path path, String rawStageAndStep) {
        var stageAndStep = rawStageAndStep.split("-");
        return new ConveyorTaskBinding(
            Stage.valueOf(stageAndStep[0]),
            Step.valueOf(stageAndStep[1]),
            (conveyorSchematic, products) -> execute(path)
        );
    }

    private Set<Product> execute(Path path) {
        sleep();
        writeInstant(path);
        return Set.of();
    }

    private void sleep() {
        try {
            TimeUnit.MILLISECONDS.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void writeInstant(Path path) {
        try {
            Files.createDirectories(path.getParent());
            Files.writeString(path, Instant.now().toString());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}

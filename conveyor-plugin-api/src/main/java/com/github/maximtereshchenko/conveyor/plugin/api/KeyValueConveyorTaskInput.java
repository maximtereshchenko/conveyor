package com.github.maximtereshchenko.conveyor.plugin.api;

public record KeyValueConveyorTaskInput(String key, String value) implements ConveyorTaskInput {

    @Override
    public int compareTo(ConveyorTaskInput input) {
        return switch (input) {
            case KeyValueConveyorTaskInput keyValueInput -> compareTo(keyValueInput);
            case PathConveyorTaskInput ignored -> -1;
        };
    }

    private int compareTo(KeyValueConveyorTaskInput keyValueInput) {
        var byKey = key.compareTo(keyValueInput.key());
        if (byKey == 0) {
            return value.compareTo(keyValueInput.value());
        }
        return byKey;
    }
}

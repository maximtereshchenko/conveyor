package com.github.maximtereshchenko.conveyor.domain;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

final class ImmutableMap<K, V> {

    private final Map<K, V> map;

    ImmutableMap(Map<K, V> map) {
        this.map = Map.copyOf(map);
    }

    ImmutableMap() {
        this(Map.of());
    }

    Stream<Map.Entry<K, V>> stream() {
        return map.entrySet().stream();
    }

    ImmutableMap<K, V> compute(K key, Supplier<V> supplier, UnaryOperator<V> action) {
        var copy = new HashMap<>(map);
        copy.put(key, action.apply(copy.computeIfAbsent(key, ignored -> supplier.get())));
        return new ImmutableMap<>(copy);
    }

    ImmutableMap<K, V> computeIfAbsent(K key, Supplier<V> supplier) {
        return compute(key, supplier, value -> value);
    }

    Optional<V> value(K key) {
        return Optional.ofNullable(map.get(key));
    }

    ImmutableSet<K> keys() {
        return new ImmutableSet<>(map.keySet());
    }

    ImmutableMap<K, V> with(K key, V value) {
        var copy = new HashMap<>(map);
        copy.put(key, value);
        return new ImmutableMap<>(copy);
    }

    ImmutableCollection<V> values() {
        return new ImmutableList<>(map.values());
    }

    ImmutableMap<K, V> withAll(ImmutableMap<K, V> immutableMap) {
        var copy = new HashMap<>(map);
        copy.putAll(immutableMap.map);
        return new ImmutableMap<>(copy);
    }
}

package com.github.maximtereshchenko.conveyor.core;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Stream;

final class Version implements Comparable<Version> {

    private final String raw;

    Version(String raw) {
        this.raw = raw;
    }

    @Override
    public int compareTo(Version version) {
        var byVersionComponents = compare(versionComponents(), version.versionComponents());
        if (byVersionComponents != 0) {
            return byVersionComponents;
        }
        var thisQualifiers = qualifiers();
        var otherQualifiers = version.qualifiers();
        if (thisQualifiers.isEmpty() && !otherQualifiers.isEmpty()) {
            return 1;
        }
        if (otherQualifiers.isEmpty() && !thisQualifiers.isEmpty()) {
            return -1;
        }
        return compare(thisQualifiers, otherQualifiers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(raw);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        var that = (Version) object;
        return Objects.equals(raw, that.raw);
    }

    @Override
    public String toString() {
        return raw;
    }

    private int compare(List<Identifier> thisTokens, List<Identifier> otherTokens) {
        var index = 0;
        while (index < thisTokens.size() && index < otherTokens.size()) {
            var comparison = thisTokens.get(index).compareTo(otherTokens.get(index));
            if (comparison != 0) {
                return comparison;
            }
            index++;
        }
        if (index < thisTokens.size()) {
            return 1;
        }
        if (index < otherTokens.size()) {
            return -1;
        }
        return 0;
    }

    private List<Identifier> identifiers(String string) {
        return Stream.of(string.split("\\.|-|(?<=[a-z])(?=\\d)"))
            .map(this::identifier)
            .toList();
    }

    private Identifier identifier(String token) {
        try {
            return new NumberIdentifier(Integer.parseInt(token));
        } catch (NumberFormatException ignored) {
            return new StringIdentifier(token.toLowerCase(Locale.ROOT));
        }
    }

    private List<Identifier> versionComponents() {
        return identifiers(versionComponentsPart());
    }

    private String versionComponentsPart() {
        var index = indexOfDash();
        if (index == -1) {
            return raw;
        }
        return raw.substring(0, index);
    }

    private List<Identifier> qualifiers() {
        var index = indexOfDash();
        if (index == -1) {
            return List.of();
        }
        return identifiers(raw.substring(index + 1));
    }

    private int indexOfDash() {
        return raw.indexOf('-');
    }

    private sealed interface Identifier extends Comparable<Identifier> {}

    private record NumberIdentifier(int value) implements Identifier {

        @Override
        public int compareTo(Identifier token) {
            return switch (token) {
                case NumberIdentifier number -> Integer.compare(value, number.value());
                case StringIdentifier ignored -> -1;
            };
        }
    }

    private record StringIdentifier(String value) implements Identifier {

        @Override
        public int compareTo(Identifier token) {
            return switch (token) {
                case NumberIdentifier ignored -> 1;
                case StringIdentifier string -> value.compareTo(string.value());
            };
        }
    }
}

package com.github.maximtereshchenko.conveyor.domain;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;

final class SemanticVersion implements Comparable<SemanticVersion> {

    private final String raw;

    SemanticVersion(String raw) {
        this.raw = raw;
    }

    @Override
    public int compareTo(SemanticVersion semanticVersion) {
        var byVersions = compareByVersions(semanticVersion);
        if (byVersions != 0) {
            return byVersions;
        }
        var thisPreReleaseIdentifiers = preReleaseIdentifiers();
        var otherPreReleaseIdentifiers = semanticVersion.preReleaseIdentifiers();
        var comparedByPreReleaseVersion = compareByPreReleaseVersion(
            thisPreReleaseIdentifiers,
            otherPreReleaseIdentifiers
        );
        if (comparedByPreReleaseVersion != 0) {
            return comparedByPreReleaseVersion;
        }
        return compareByPreReleaseIdentifiers(thisPreReleaseIdentifiers, otherPreReleaseIdentifiers);
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
        var that = (SemanticVersion) object;
        return Objects.equals(raw, that.raw);
    }

    @Override
    public String toString() {
        return raw;
    }

    private int compareByPreReleaseIdentifiers(
        String[] preReleaseIdentifiers,
        String[] otherPreReleaseIdentifiers
    ) {
        for (int i = 0; i < preReleaseIdentifiers.length && i < otherPreReleaseIdentifiers.length; i++) {
            var identifier = preReleaseIdentifiers[i];
            var otherIdentifier = otherPreReleaseIdentifiers[i];
            var comparedNumericallyOrLexically = compareNumericallyOrLexically(identifier, otherIdentifier);
            if (comparedNumericallyOrLexically != 0) {
                return comparedNumericallyOrLexically;
            }
        }
        return Integer.compare(preReleaseIdentifiers.length, otherPreReleaseIdentifiers.length);
    }

    private int compareNumericallyOrLexically(String identifier, String otherIdentifier) {
        return number(identifier)
            .map(identifierNumber ->
                number(otherIdentifier)
                    .map(otherIdentifierNumber -> Integer.compare(identifierNumber, otherIdentifierNumber))
                    .orElse(-1)
            )
            .orElseGet(() -> identifier.compareTo(otherIdentifier));
    }

    private Optional<Integer> number(String possibleNumber) {
        try {
            return Optional.of(Integer.valueOf(possibleNumber));
        } catch (NumberFormatException ignored) {
            return Optional.empty();
        }
    }

    private int compareByPreReleaseVersion(String[] preReleaseIdentifiers, String[] otherPreReleaseIdentifiers) {
        if (preReleaseIdentifiers.length == 0 && otherPreReleaseIdentifiers.length != 0) {
            return 1;
        }
        if (otherPreReleaseIdentifiers.length == 0 && preReleaseIdentifiers.length != 0) {
            return -1;
        }
        return 0;
    }

    private int compareByVersions(SemanticVersion semanticVersion) {
        return Comparator.comparingInt(SemanticVersion::major)
            .thenComparingInt(SemanticVersion::minor)
            .thenComparingInt(SemanticVersion::patch)
            .compare(this, semanticVersion);
    }

    private int major() {
        return numberAt(0);
    }

    private int minor() {
        return numberAt(1);
    }

    private int patch() {
        return Integer.parseInt(patchAndPreRelease().split("-")[0]);
    }

    private String[] preReleaseIdentifiers() {
        var patchAndPreRelease = patchAndPreRelease().split("-");
        if (patchAndPreRelease.length == 1) {
            return new String[0];
        }
        return dotSeparated(patchAndPreRelease[1]);
    }

    private String patchAndPreRelease() {
        var dotSeparated = dotSeparated(raw);
        return String.join(".", Arrays.copyOfRange(dotSeparated, 2, dotSeparated.length))
            .split("\\+")[0];
    }

    private int numberAt(int index) {
        return Integer.parseInt(dotSeparated(raw)[index]);
    }

    private String[] dotSeparated(String line) {
        return line.split("\\.");
    }
}

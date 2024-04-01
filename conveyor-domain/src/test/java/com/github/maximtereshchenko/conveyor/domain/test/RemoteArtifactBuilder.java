package com.github.maximtereshchenko.conveyor.domain.test;

import com.github.maximtereshchenko.conveyor.jackson.dataformat.xml.XmlMapper;
import com.github.maximtereshchenko.conveyor.wiremock.client.WireMock;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.maximtereshchenko.conveyor.wiremock.client.WireMock.get;
import static com.github.maximtereshchenko.conveyor.wiremock.client.WireMock.ok;

final class RemoteArtifactBuilder {

    private final List<String> groupId;
    private final String artifactId;
    private final String version;
    private final String classifier;
    private final byte[] content;
    private final String contentType;
    private final XmlMapper xmlMapper;

    private RemoteArtifactBuilder(
        List<String> groupId,
        String artifactId,
        String version,
        String classifier,
        byte[] content,
        String contentType,
        XmlMapper xmlMapper
    ) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.classifier = classifier;
        this.content = content;
        this.contentType = contentType;
        this.xmlMapper = xmlMapper;
    }

    RemoteArtifactBuilder(XmlMapper xmlMapper) {
        this(List.of(), "", "", "", new byte[0], "", xmlMapper);
    }

    RemoteArtifactBuilder groupId(String... elements) {
        return new RemoteArtifactBuilder(
            List.of(elements),
            artifactId,
            version,
            classifier,
            content,
            contentType,
            xmlMapper
        );
    }

    RemoteArtifactBuilder artifactId(String artifactId) {
        return new RemoteArtifactBuilder(
            groupId,
            artifactId,
            version,
            classifier,
            content,
            contentType,
            xmlMapper
        );
    }

    RemoteArtifactBuilder version(String version) {
        return new RemoteArtifactBuilder(
            groupId,
            artifactId,
            version,
            classifier,
            content,
            contentType,
            xmlMapper
        );
    }

    RemoteArtifactBuilder jar(String templateDirectory) {
        return new RemoteArtifactBuilder(
            groupId,
            artifactId,
            version,
            "jar",
            JarBuilder.from(templateDirectory)
                .name(String.join(".", groupId) + ':' + artifactId)
                .version(version)
                .bytes(),
            "application/octet-stream",
            xmlMapper
        );
    }

    RemoteArtifactBuilder pom(PomModel pomModel) {
        var byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            byteArrayOutputStream.write(
                """
                <?xml version="1.0" encoding="UTF-8"?>
                """.getBytes(StandardCharsets.UTF_8)
            );
            xmlMapper.writerWithDefaultPrettyPrinter().writeValue(byteArrayOutputStream, pomModel);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return new RemoteArtifactBuilder(
            groupId,
            artifactId,
            version,
            "pom",
            byteArrayOutputStream.toByteArray(),
            "text/xml",
            xmlMapper
        );
    }

    void install(WireMock wireMock) {
        wireMock.register(
            get(
                Stream.concat(
                        groupId.stream(),
                        Stream.of(
                            artifactId,
                            version,
                            "%s-%s.%s".formatted(artifactId, version, classifier)
                        )
                    )
                    .collect(Collectors.joining("/", "/", ""))
            )
                .willReturn(
                    ok()
                        .withHeader("Content-Type", contentType)
                        .withBody(content)
                )
        );
    }
}

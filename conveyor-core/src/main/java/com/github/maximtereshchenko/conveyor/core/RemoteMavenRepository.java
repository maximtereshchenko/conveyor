package com.github.maximtereshchenko.conveyor.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.util.Optional;

final class RemoteMavenRepository extends UriRepository<Path, Resource> {

    private final Tracer tracer;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    RemoteMavenRepository(URI baseUri, Tracer tracer) {
        super(baseUri);
        this.tracer = tracer;
    }

    @Override
    void publish(URI uri, Path artifact) {
        throw new UnsupportedOperationException();
    }

    @Override
    Optional<Resource> artifact(URI uri) {
        var response = getResponse(uri);
        if (response.statusCode() != 200) {
            return Optional.empty();
        }
        tracer.submitDownloadedArtifact(uri);
        return Optional.of(new Resource(response::body));
    }

    private HttpResponse<InputStream> getResponse(URI uri) {
        try {
            return httpClient.send(
                HttpRequest.newBuilder()
                    .GET()
                    .uri(uri)
                    .build(),
                HttpResponse.BodyHandlers.ofInputStream()
            );
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}

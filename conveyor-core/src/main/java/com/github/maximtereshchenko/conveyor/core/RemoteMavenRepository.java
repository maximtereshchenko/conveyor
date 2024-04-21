package com.github.maximtereshchenko.conveyor.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;

final class RemoteMavenRepository implements UriRepository<InputStream> {

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final URI baseUri;

    RemoteMavenRepository(URI baseUri) {
        this.baseUri = baseUri;
    }

    @Override
    public Optional<InputStream> artifact(URI uri) {
        var response = getResponse(URI.create(baseUri.toString() + '/' + uri));
        if (response.statusCode() != 200) {
            return Optional.empty();
        }
        return Optional.of(response.body());
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
            throw new IllegalArgumentException(e);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}

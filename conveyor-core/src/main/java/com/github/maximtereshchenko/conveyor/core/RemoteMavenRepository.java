package com.github.maximtereshchenko.conveyor.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;

final class RemoteMavenRepository extends UriRepository<InputStream> {

    private static final System.Logger LOGGER =
        System.getLogger(RemoteMavenRepository.class.getName());

    private final String name;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    RemoteMavenRepository(String name, URI baseUri) {
        super(baseUri);
        this.name = name;
    }

    @Override
    public Optional<InputStream> artifact(URI uri) {
        var response = getResponse(uri);
        if (response.statusCode() != 200) {
            return Optional.empty();
        }
        LOGGER.log(System.Logger.Level.INFO, "Downloaded from {0}: {1}", name, uri);
        return Optional.of(response.body());
    }

    @Override
    void publish(URI uri, Resource resource) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean hasName(String name) {
        return this.name.equals(name);
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

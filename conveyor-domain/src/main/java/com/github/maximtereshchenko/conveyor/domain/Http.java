package com.github.maximtereshchenko.conveyor.domain;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;

final class Http {

    private final HttpClient httpClient = HttpClient.newHttpClient();

    Optional<InputStream> get(URI uri) {
        var response = getResponse(uri);
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

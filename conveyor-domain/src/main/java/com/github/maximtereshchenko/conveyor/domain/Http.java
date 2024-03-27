package com.github.maximtereshchenko.conveyor.domain;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;
import java.util.function.Function;

final class Http {

    private final HttpClient httpClient = HttpClient.newHttpClient();

    <T> Optional<T> get(URI uri, Function<InputStream, T> function) {
        var response = getResponse(uri);
        if (response.statusCode() != 200) {
            return Optional.empty();
        }
        try (var inputStream = response.body()) {
            return Optional.of(function.apply(inputStream));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
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

package com.github.maximtereshchenko.conveyor.core;

import java.net.URI;
import java.util.Optional;

interface UriRepository<T> {

    Optional<T> artifact(URI uri);
}

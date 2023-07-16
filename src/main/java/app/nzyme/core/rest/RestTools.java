/*
 * This file is part of nzyme.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the Server Side Public License, version 1,
 * as published by MongoDB, Inc.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * Server Side Public License for more details.
 *
 * You should have received a copy of the Server Side Public License
 * along with this program. If not, see
 * <http://www.mongodb.com/licensing/server-side-public-license>.
 */

package app.nzyme.core.rest;

import com.google.common.base.Strings;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.core.MultivaluedMap;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

public class RestTools {

    public static final String OVERRIDE_HEADER = "X-Nzyme-URL";

    public static URI buildExternalUri(@NotNull MultivaluedMap<String, String> httpHeaders, @NotNull URI defaultUri) {
        Optional<URI> externalUri = Optional.empty();
        final List<String> headers = httpHeaders.get(OVERRIDE_HEADER);
        if (headers != null && !headers.isEmpty()) {
            externalUri = headers.stream()
                    .filter(s -> {
                        try {
                            if (Strings.isNullOrEmpty(s)) {
                                return false;
                            }
                            final URI uri = new URI(s);
                            if (!uri.isAbsolute()) {
                                return true;
                            }
                            switch (uri.getScheme()) {
                                case "http":
                                case "https":
                                    return true;
                            }
                            return false;
                        } catch (URISyntaxException e) {
                            return false;
                        }
                    })
                    .map(URI::create)
                    .findFirst();
        }

        final URI uri = externalUri.orElse(defaultUri);

        // Make sure we return an URI object with a trailing slash
        if (!uri.toString().endsWith("/")) {
            return URI.create(uri.toString() + "/");
        }
        return uri;
    }

}

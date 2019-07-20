/*
 *  This file is part of nzyme.
 *
 *  nzyme is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  nzyme is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with nzyme.  If not, see <http://www.gnu.org/licenses/>.
 */

package horse.wtf.nzyme.rest;

import com.google.common.base.Strings;

import javax.validation.constraints.NotNull;
import javax.ws.rs.core.MultivaluedMap;
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

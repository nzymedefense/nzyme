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

package horse.wtf.nzyme.rest.web;

import com.floreysoft.jmte.Engine;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.Resources;
import horse.wtf.nzyme.configuration.Configuration;
import horse.wtf.nzyme.rest.RestTools;

import javax.ws.rs.core.MultivaluedMap;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static java.util.Objects.requireNonNull;

public class IndexHtmlGenerator {

    private final String template;
    private final Configuration configuration;
    private final Engine templateEngine;

    private final List<String> jsFiles;
    private final List<String> cssFiles;

    public IndexHtmlGenerator(final Configuration configuration, AssetManifest assetManifest) throws IOException {
        this(
                Resources.toString(Resources.getResource("web-interface/index.html.template"), StandardCharsets.UTF_8),
                assetManifest.getJsFiles(),
                assetManifest.getCssFiles(),
                Engine.createEngine(),
                configuration
        );
    }

    private IndexHtmlGenerator(final String template,
                               final List<String> jsFiles,
                               final List<String> cssFiles,
                               final Engine templateEngine,
                               final Configuration configuration) throws IOException {
        this.template = requireNonNull(template, "template");
        this.jsFiles = requireNonNull(jsFiles, "jsFiles");
        this.cssFiles = requireNonNull(cssFiles, "cssFiles");
        this.templateEngine = requireNonNull(templateEngine, "templateEngine");
        this.configuration = requireNonNull(configuration, "configuration");
    }

    public String get(MultivaluedMap<String, String> headers) {
        final Map<String, Object> model = ImmutableMap.<String, Object>builder()
                .put("title", "nzyme - WiFi Defense System")
                .put("jsFiles", jsFiles)
                .put("cssFiles", cssFiles)
                .put("appPrefix", RestTools.buildExternalUri(headers, configuration.httpExternalUri()))
                .put("apiUri", RestTools.buildExternalUri(headers, configuration.httpExternalUri()))
                .build();
        return templateEngine.transform(template, model);
    }

}

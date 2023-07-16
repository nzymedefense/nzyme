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

package app.nzyme.core.rest.web;

import app.nzyme.core.configuration.node.NodeConfiguration;
import com.floreysoft.jmte.Engine;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.Resources;
import app.nzyme.core.rest.RestTools;
import jakarta.ws.rs.core.MultivaluedMap;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static java.util.Objects.requireNonNull;

public class IndexHtmlGenerator {

    private final String template;
    private final NodeConfiguration configuration;
    private final Engine templateEngine;

    private final List<String> jsFiles;
    private final List<String> cssFiles;

    public IndexHtmlGenerator(final NodeConfiguration configuration, AssetManifest assetManifest) throws IOException {
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
                               final NodeConfiguration configuration) throws IOException {
        this.template = requireNonNull(template, "template");
        this.jsFiles = requireNonNull(jsFiles, "jsFiles");
        this.cssFiles = requireNonNull(cssFiles, "cssFiles");
        this.templateEngine = requireNonNull(templateEngine, "templateEngine");
        this.configuration = requireNonNull(configuration, "configuration");
    }

    public String get(MultivaluedMap<String, String> headers) {
        final Map<String, Object> model = ImmutableMap.<String, Object>builder()
                .put("title", "nzyme - Network Defense System")
                .put("jsFiles", jsFiles)
                .put("cssFiles", cssFiles)
                .put("appPrefix", RestTools.buildExternalUri(headers, configuration.httpExternalUri()))
                .put("apiUri", RestTools.buildExternalUri(headers, configuration.httpExternalUri()))
                .build();
        return templateEngine.transform(template, model);
    }

}

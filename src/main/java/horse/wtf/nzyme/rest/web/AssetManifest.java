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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AssetManifest {

    private final List<String> jsFiles;
    private final List<String> cssFiles;

    public AssetManifest() {
        this.jsFiles = Lists.newArrayList();
        this.cssFiles = Lists.newArrayList();

        ObjectMapper om = new ObjectMapper();
        om.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);

        InputStream vendorManifestStream = this.getClass().getResourceAsStream("/web-interface/assets/asset-manifest.json");
        if (vendorManifestStream != null) {
            Map<String, String> manifest;
            try {
                manifest = om.readValue(vendorManifestStream, new TypeReference<HashMap<String, String>>() {});
            } catch (IOException e) {
                throw new RuntimeException("Unable to read asset manifest.", e);
            }

            for (String x : manifest.values()) {
                if (x.endsWith(".css")) {
                    this.cssFiles.add(x.split("/")[2]);
                }

                if (x.endsWith(".js")) {
                    this.jsFiles.add(x.split("/")[2]);
                }
            }

        } else {
            throw new IllegalStateException("Unable to find vendor assets.");
        }
    }

    public List<String> getJsFiles() {
        return jsFiles;
    }

    public List<String> getCssFiles() {
        return cssFiles;
    }

}

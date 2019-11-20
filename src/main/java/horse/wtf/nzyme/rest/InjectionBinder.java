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

import com.fasterxml.jackson.databind.ObjectMapper;
import horse.wtf.nzyme.Nzyme;
import horse.wtf.nzyme.rest.web.AssetManifest;
import horse.wtf.nzyme.rest.web.IndexHtmlGenerator;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

import javax.activation.MimetypesFileTypeMap;
import java.io.IOException;

public class InjectionBinder extends AbstractBinder {

    private final Nzyme nzyme;

    public InjectionBinder(Nzyme nzmye) {
        this.nzyme = nzmye;
    }

    @Override
    protected void configure() {
        bind(nzyme).to(Nzyme.class);
        bind(new MimetypesFileTypeMap()).to(MimetypesFileTypeMap.class);
        bind(nzyme.getObjectMapper()).to(ObjectMapper.class);

        try {
            bind(new IndexHtmlGenerator(nzyme.getConfiguration(), new AssetManifest())).to(IndexHtmlGenerator.class);
        } catch (IOException e) {
            throw new RuntimeException("Could not bind IndexHtmlGenerator.", e);
        }
    }

}

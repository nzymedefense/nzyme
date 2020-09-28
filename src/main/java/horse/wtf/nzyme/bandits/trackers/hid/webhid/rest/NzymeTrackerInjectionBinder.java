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

package horse.wtf.nzyme.bandits.trackers.hid.webhid.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import horse.wtf.nzyme.NzymeTracker;
import horse.wtf.nzyme.bandits.trackers.hid.webhid.WebHID;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

public class NzymeTrackerInjectionBinder extends AbstractBinder  {

    private final NzymeTracker nzyme;
    private final WebHID webHID;

    public NzymeTrackerInjectionBinder(NzymeTracker nzmye, WebHID webHID) {
        this.nzyme = nzmye;
        this.webHID = webHID;
    }

    @Override
    protected void configure() {
        bind(nzyme).to(NzymeTracker.class);
        bind(webHID).to(WebHID.class);
        bind(nzyme.getObjectMapper()).to(ObjectMapper.class);
    }

}

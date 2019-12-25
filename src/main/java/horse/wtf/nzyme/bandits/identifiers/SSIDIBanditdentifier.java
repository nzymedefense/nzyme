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

package horse.wtf.nzyme.bandits.identifiers;

import com.google.common.base.Joiner;
import horse.wtf.nzyme.dot11.frames.Dot11BeaconFrame;
import horse.wtf.nzyme.dot11.frames.Dot11DeauthenticationFrame;
import horse.wtf.nzyme.dot11.frames.Dot11ProbeResponseFrame;

import java.util.List;
import java.util.Optional;

public class SSIDIBanditdentifier implements BanditIdentifier {

    private final List<String> ssids;
    private final String listDescription;

    public SSIDIBanditdentifier(List<String> ssids) {
        this.ssids = ssids;
        this.listDescription = Joiner.on(",").join(ssids);
    }

    @Override
    public Descriptor descriptor() {
        return Descriptor.create(
                TYPE.SSID,
                "Matches if the SSID advertised by frame is one of the configured SSIDs. (multiple SSIDs can be entered, separated by comma)",
                "frame.ssid IN [" + listDescription + "]"
        );
    }


    @Override
    public Optional<Boolean> matches(Dot11DeauthenticationFrame frame) {
        return Optional.empty();
    }

    @Override
    public Optional<Boolean> matches(Dot11BeaconFrame frame) {
        return match(frame.ssid());
    }

    @Override
    public Optional<Boolean> matches(Dot11ProbeResponseFrame frame) {
        return match(frame.ssid());
    }

    private Optional<Boolean> match(String ssid) {
        return Optional.of(ssids.contains(ssid));
    }

}

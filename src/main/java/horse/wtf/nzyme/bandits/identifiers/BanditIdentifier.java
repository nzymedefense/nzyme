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

import horse.wtf.nzyme.dot11.frames.Dot11BeaconFrame;
import horse.wtf.nzyme.dot11.frames.Dot11DeauthenticationFrame;
import horse.wtf.nzyme.dot11.frames.Dot11ProbeResponseFrame;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public abstract class BanditIdentifier {

    public enum TYPE {
        FINGERPRINT,
        SSID,
        SIGNAL_STRENGTH,
        PWNAGOTCHI_IDENTITY
    }

    public abstract BanditIdentifierDescriptor descriptor();
    public abstract Map<String, Object> configuration();

    public abstract Optional<Boolean> matches(Dot11DeauthenticationFrame frame);
    public abstract Optional<Boolean> matches(Dot11BeaconFrame frame);
    public abstract Optional<Boolean> matches(Dot11ProbeResponseFrame frame);

    private Long databaseID;
    private UUID uuid;

    public BanditIdentifier(Long databaseID, UUID uuid) {
        this.databaseID = databaseID;
        this.uuid = uuid;
    }

    public long getDatabaseID() {
        return databaseID;
    }

    public UUID getUuid() {
        return uuid;
    }

}

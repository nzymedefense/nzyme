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
import horse.wtf.nzyme.notifications.FieldNames;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class FingerprintBanditIdentifier extends BanditIdentifier {

    private final String fingerprint;

    public FingerprintBanditIdentifier(String fingerprint, Long databaseID, UUID uuid) {
        super(databaseID, uuid);

        this.fingerprint = fingerprint;
    }

    public String getFingerprint() {
        return fingerprint;
    }

    @Override
    public BanditIdentifierDescriptor descriptor() {
        return BanditIdentifierDescriptor.create(
                TYPE.FINGERPRINT,
                "Matches if the frame fingerprint equals the expected fingerprint.",
                "frame.fingerprint == \"" + fingerprint + "\""
        );
    }

    @Override
    public Map<String, Object> configuration() {
        return new HashMap<String, Object>(){{
            put(FieldNames.FINGERPRINT, fingerprint);
        }};
    }

    @Override
    public Optional<Boolean> matches(Dot11DeauthenticationFrame frame) {
        return Optional.empty();
    }

    @Override
    public Optional<Boolean> matches(Dot11BeaconFrame frame) {
        if("getoffmylawn_tetra2".equals(frame.ssid())) {
            System.out.println(frame.transmitterFingerprint());
        }

        return match(frame.transmitterFingerprint());
    }

    @Override
    public Optional<Boolean> matches(Dot11ProbeResponseFrame frame) {
        return match(frame.transmitterFingerprint());
    }

    private Optional<Boolean> match(String receivedFingerprint) {
        if(receivedFingerprint == null) {
            return Optional.of(false);
        }

        return Optional.of(receivedFingerprint.equals(fingerprint));
    }

}

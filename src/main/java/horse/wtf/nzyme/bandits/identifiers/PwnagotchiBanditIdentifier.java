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

import com.google.common.base.Strings;
import horse.wtf.nzyme.dot11.frames.Dot11BeaconFrame;
import horse.wtf.nzyme.dot11.frames.Dot11DeauthenticationFrame;
import horse.wtf.nzyme.dot11.frames.Dot11ProbeResponseFrame;
import horse.wtf.nzyme.dot11.interceptors.misc.PwnagotchiAdvertisement;
import horse.wtf.nzyme.dot11.misc.PwnagotchiAdvertisementExtractor;
import horse.wtf.nzyme.notifications.FieldNames;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class PwnagotchiBanditIdentifier extends BanditIdentifier {

    private final String identity;
    private final PwnagotchiAdvertisementExtractor extractor;

    public PwnagotchiBanditIdentifier(String identity, Long databaseID, UUID uuid) {
        super(databaseID, uuid);

        this.extractor = new PwnagotchiAdvertisementExtractor();
        this.identity = identity;
    }

    @Override
    public BanditIdentifierDescriptor descriptor() {
        return BanditIdentifierDescriptor.create(
                TYPE.PWNAGOTCHI_IDENTITY,
                "Matches if the frame is a Pwnagotchi advertisement for the expected Pwnagotchi identity.",
                "frame.pwnagotchi_identity == \"" + identity + "\""
        );
    }

    @Override
    public Map<String, Object> configuration() {
        return new HashMap<String, Object>(){{
            put(FieldNames.IDENTITY, identity);
        }};
    }

    @Override
    public Optional<Boolean> matches(Dot11DeauthenticationFrame frame) {
        return Optional.empty();
    }

    @Override
    public Optional<Boolean> matches(Dot11BeaconFrame frame) {
        Optional<PwnagotchiAdvertisement> result = extractor.extract(frame);
        return Optional.of(result.isPresent() && !Strings.isNullOrEmpty(result.get().identity()) && result.get().identity().equals(identity));
    }

    @Override
    public Optional<Boolean> matches(Dot11ProbeResponseFrame frame) {
        return Optional.empty();
    }

}

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

package app.nzyme.core.bandits.identifiers;

import com.google.common.base.Strings;
import app.nzyme.core.dot11.frames.Dot11BeaconFrame;
import app.nzyme.core.dot11.frames.Dot11DeauthenticationFrame;
import app.nzyme.core.dot11.frames.Dot11ProbeResponseFrame;
import app.nzyme.core.dot11.interceptors.misc.PwnagotchiAdvertisement;
import app.nzyme.core.dot11.misc.PwnagotchiAdvertisementExtractor;
import app.nzyme.core.notifications.FieldNames;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class PwnagotchiBanditIdentifier extends BanditIdentifier {

    private final String identity;
    private final PwnagotchiAdvertisementExtractor extractor;

    public PwnagotchiBanditIdentifier(String identity, Long databaseID, UUID uuid) {
        super(databaseID, uuid, TYPE.PWNAGOTCHI_IDENTITY);

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

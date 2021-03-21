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

package horse.wtf.nzyme.dot11.handlers;

import com.google.common.collect.ImmutableList;
import horse.wtf.nzyme.dot11.Dot11MetaInformation;
import horse.wtf.nzyme.dot11.probes.Dot11ProbeConfiguration;

import java.util.Collections;

public class FrameHandlerTest {

    protected static final Dot11MetaInformation META_NO_WEP = new Dot11MetaInformation(false, 100, 2400, 1, 0L, false);

    protected static final Dot11ProbeConfiguration CONFIG_STANDARD = Dot11ProbeConfiguration.create(
            "mockProbe1",
            ImmutableList.of(),
            "test1",
            "wlan0",
            ImmutableList.of(),
            1,
            "foo",
            ImmutableList.of(),
            ImmutableList.of()
    );

}

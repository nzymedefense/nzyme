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

package app.nzyme.core.dot11;

import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class Dot11LeavingReasonTest {

    @Test
    public void testLookup() throws Exception {
        assertEquals(
                Dot11LeavingReason.lookup(2),
                "Previous authentication no longer valid"
        );

        assertEquals(
                Dot11LeavingReason.lookup(9001),
                "Unknown reason (9001)"
        );
    }

}
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

package horse.wtf.nzyme;

import com.google.common.collect.Maps;

import java.util.Map;

public class MemoryRegistry {

    // TODO probably move this to the new DB-backed registry.

    public enum KEY {
        NEW_VERSION_AVAILABLE
    }

    private final Map<KEY, Boolean> booleans;

    public MemoryRegistry() {
        this.booleans = Maps.newConcurrentMap();
    }

    public boolean getBool(KEY key) {
        return booleans.getOrDefault(key, false);
    }

    public void setBool(KEY key, boolean value) {
        booleans.put(key, value);
    }

}

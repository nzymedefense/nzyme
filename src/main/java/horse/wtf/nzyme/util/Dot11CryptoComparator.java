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

package horse.wtf.nzyme.util;

import com.google.common.collect.Lists;
import horse.wtf.nzyme.dot11.interceptors.CryptoChangeInterceptorSet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class Dot11CryptoComparator {

    private static final Logger LOG = LogManager.getLogger(CryptoChangeInterceptorSet.class);

    public static boolean compareSecurity(List<String> encountered, List<String> expected) {
        if (expected.size() != encountered.size()) {
            LOG.debug("Unexpected security settings: expected size <{}> does not match encountered size <{}>.", expected.size(), encountered.size());
            return false;
        }

        final AtomicBoolean result = new AtomicBoolean(true);
        List<String> seen = Lists.newArrayList();
        encountered.forEach((e) -> {
            if(seen.contains(e)) {
                LOG.warn("Unexpected security settings: Encountered contains duplicate security [{}]. This is not 802.11 compliant and highly unusual.", e);
                result.set(false);
            }
        });

        if(!result.get()) {
            return false;
        }

        expected.forEach((e) -> {
            if (!encountered.contains(e)) {
                LOG.debug("Unexpected security settings: Expected security [{}] not in encountered [{}].", e, encountered);
                result.set(false);
            }
        });

        return result.get();
    }

}

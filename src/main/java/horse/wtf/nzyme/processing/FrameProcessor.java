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

package horse.wtf.nzyme.processing;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import horse.wtf.nzyme.dot11.Dot11FrameInterceptor;
import horse.wtf.nzyme.dot11.frames.Dot11Frame;

import java.util.List;

public class FrameProcessor {

    private final List<Dot11FrameInterceptor> dot11Interceptors;

    public FrameProcessor() {
        this.dot11Interceptors = Lists.newArrayList();
    }

    public void processDot11Frame(Dot11Frame frame) {
        for (Dot11FrameInterceptor interceptor : dot11Interceptors) {
            if (interceptor.forSubtype() == frame.frameType()) {
                interceptor.intercept(frame);
            }
        }
    }

    public void registerDot11Interceptors(List<Dot11FrameInterceptor> interceptors) {
        for (Dot11FrameInterceptor interceptor : interceptors) {
            registerDot11Interceptor(interceptor);
        }
    }

    public void registerDot11Interceptor(Dot11FrameInterceptor interceptor) {
        this.dot11Interceptors.add(interceptor);
    }

    public ImmutableList<Dot11FrameInterceptor> getDot11Interceptors() {
        return ImmutableList.copyOf(this.dot11Interceptors);
    }

}

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
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import horse.wtf.nzyme.dot11.Dot11FrameInterceptor;
import horse.wtf.nzyme.dot11.frames.Dot11Frame;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class FrameProcessor {

    private final AtomicLong recentDot11FrameCount;
    private final AtomicLong recentDot11FrameCountTemp;

    private final List<Dot11FrameInterceptor> dot11Interceptors;

    public FrameProcessor() {
        this.dot11Interceptors = Lists.newArrayList();

        this.recentDot11FrameCount = new AtomicLong(0);
        this.recentDot11FrameCountTemp = new AtomicLong(0);

        // Periodically clean up recent statistics.
        Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder()
                .setDaemon(true)
                .setNameFormat("frameproc-recent-cleaner-%d")
                .build())
                .scheduleAtFixedRate(this::resetRecentDot11FrameCount, 1, 1, TimeUnit.MINUTES);
    }

    public void processDot11Frame(Dot11Frame frame) {
        recentDot11FrameCountTemp.incrementAndGet();

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

    public long getRecentFrameCount() {
        return recentDot11FrameCount.get();
    }

    public void resetRecentDot11FrameCount() {
        recentDot11FrameCount.set(recentDot11FrameCountTemp.get());
        recentDot11FrameCountTemp.set(0);
    }

}

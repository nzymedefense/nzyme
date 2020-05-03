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

package horse.wtf.nzyme.bandits.trackers.hid;

import com.google.common.collect.EvictingQueue;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import horse.wtf.nzyme.bandits.Bandit;
import horse.wtf.nzyme.bandits.trackers.TrackerState;
import horse.wtf.nzyme.bandits.trackers.protobuf.TrackerMessage;
import horse.wtf.nzyme.util.Sounds;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.sound.sampled.*;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;

public class AudioHID implements TrackerHID {

    private static final Logger LOG = LogManager.getLogger(AudioHID.class);

    private boolean wasOffline = true;
    private boolean wasOutOfSync = true;

    private final Queue<String> playbackQueue;

    public AudioHID() {
        //noinspection UnstableApiUsage
        this.playbackQueue = EvictingQueue.create(3);
    }

    @Override
    public void initialize() {
        Executors.newSingleThreadExecutor(new ThreadFactoryBuilder()
                .setDaemon(true)
                .setNameFormat("audiohid-player-%d")
                .build()).submit(() -> {
                    while (true) {
                        try {
                            while(playbackQueue.peek() != null) {
                                Thread.sleep(250);

                                CountDownLatch playerFinishLatch = new CountDownLatch(1);
                                String clipFile = playbackQueue.poll();

                                if (clipFile == null) {
                                    break;
                                }

                                LOG.debug("Playing sound [{}].", clipFile);

                                AudioInputStream stream = AudioSystem.getAudioInputStream(Sounds.getSound(clipFile));
                                AudioFormat format = stream.getFormat();
                                Clip clip = (Clip) AudioSystem.getLine(new DataLine.Info(Clip.class, format));

                                clip.addLineListener(e -> {
                                    if (e.getType() == LineEvent.Type.STOP) {
                                        playerFinishLatch.countDown();
                                        e.getLine().close();
                                    }
                                });

                                clip.open(stream);
                                clip.start();

                                playerFinishLatch.await();
                            }

                            Thread.sleep(25);
                        } catch (Exception e) {
                            LOG.error("Could not play sound.", e);
                        }
                    }
        });
    }

    @Override
    public void onConnectionStateChange(List<TrackerState> connectionState) {
        if (wasOffline && connectionState.contains(TrackerState.ONLINE)) {
            wasOffline = false;
            playClip("connected");
        }

        if (connectionState.contains(TrackerState.DARK)) {
            wasOffline = true;
            playClip("warning_leader_in_the_dark");
        }

        if (wasOutOfSync && connectionState.contains(TrackerState.ONLINE) && !connectionState.contains(TrackerState.OUT_OF_SYNC)) {
            wasOutOfSync = false;
            playClip("synchronized");
        }

        if (connectionState.contains(TrackerState.OUT_OF_SYNC)) {
            playClip("warning_out_of_sync");
        }

        if (connectionState.contains(TrackerState.WEAK)) {
            playClip("warning_weak_connection");
        }
    }

    @Override
    public void onTrackingStartRequestReceived(TrackerMessage.StartTrackRequest request) {

    }

    @Override
    public void onTrackingAbortRequestReceived(TrackerMessage.CancelTrackRequest request) {

    }

    @Override
    public void onInitialContactWithTrackedBandit(Bandit bandit) {

    }

    @Override
    public void onBanditTrace(int rssi) {

    }

    @Override
    public void onChannelSwitch(int newChannel) {

    }

    @Override
    public void onChannelWidthChange(ChannelWidthChangeDirection direction, List<Integer> activeChannels) {

    }

    @Override
    public void onPingFromLeaderReceived(TrackerMessage.Ping ping, int rssi) {

    }

    @Override
    public void onPingFromTrackerReceived(TrackerMessage.Ping ping, int rssi) {

    }

    @Override
    public void onBanditReceived(TrackerMessage.BanditBroadcast bandit) {

    }

    @Override
    public void onContactRequestReceived() {

    }

    private void playClip(String name) {
        this.playbackQueue.add(name);
    }

}

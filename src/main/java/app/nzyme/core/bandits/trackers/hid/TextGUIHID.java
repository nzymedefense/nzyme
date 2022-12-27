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

package app.nzyme.core.bandits.trackers.hid;

import com.google.auto.value.AutoValue;
import com.google.common.base.Joiner;
import com.google.common.collect.EvictingQueue;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.SimpleTheme;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.table.Table;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;
import app.nzyme.core.NzymeTracker;
import app.nzyme.core.bandits.Bandit;
import app.nzyme.core.bandits.trackers.TrackerState;
import app.nzyme.core.bandits.trackers.TrackerTrackSummary;
import app.nzyme.core.bandits.trackers.protobuf.TrackerMessage;
import app.nzyme.core.dot11.probes.Dot11MonitorProbe;
import app.nzyme.core.dot11.probes.Dot11Probe;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class TextGUIHID implements TrackerHID {

    private static final Logger LOG = LogManager.getLogger(TextGUIHID.class);

    private static final int COLS = 80;
    private static final int ROWS = 22;

    private final Label labelConnection = new Label(" DARK").setForegroundColor(TextColor.ANSI.RED);
    private final Label labelSignal = new Label(" ???").setForegroundColor(TextColor.ANSI.RED);
    private final Label labelTime = new Label("");
    private final Label labelTrackerStatus = new Label("");
    private final Label labelWiFiStatus = new Label("");
    private final Label labelWiFiChannels = new Label("").setForegroundColor(TextColor.ANSI.WHITE);
    private final Label labelDesignator = new Label("").setForegroundColor(TextColor.ANSI.WHITE);

    private final Label labelTask = new Label("");
    private final Label labelBandit = new Label("");
    private final Label labelTrack = new Label("");
    private final Label labelTrackSignal = new Label("");
    private final Label labelTrackFrames = new Label("");
    private final Label labelTrackContact = new Label("");

    private final Table<String> eventsTable = new Table<>("Timestamp", "Source", "Event");

    @SuppressWarnings("UnstableApiUsage")
    private final EvictingQueue<Event> events;

    private final NzymeTracker nzyme;

    public TextGUIHID(NzymeTracker nzyme) {
        this.nzyme = nzyme;

        //noinspection UnstableApiUsage
        this.events = EvictingQueue.create(5);
    }

    @Override
    public void initialize() {
        Executors.newSingleThreadExecutor(new ThreadFactoryBuilder()
                .setDaemon(true)
                .setNameFormat("textguihid-%d").build())
                .submit(() -> {
                    try {
                        /* Wait five seconds. Cheap hack to wait for any other stuff written
                         * to STDOUT and to avoid interference with drawing of the GUI.
                         */
                        Thread.sleep(5000);
                        initializeGUI();
                    } catch (Exception e) {
                        LOG.error("Could not initialize Text GUI HID.", e);
                    }
                });

        Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder()
                .setDaemon(true)
                .setNameFormat("textguihid-secup-%d").build())
                .scheduleWithFixedDelay(() -> {
                    labelTime.setText(DateTime.now().toString(DateTimeFormat.forPattern("HH:mm:ss")));
                    labelWiFiChannels.setText(wifiChannels());

                    boolean allProbesLive = true;
                    Dot11MonitorProbe firstMonitor = null;
                    for (Dot11Probe probe : nzyme.getProbes()) {
                        if (!probe.isInLoop() || !probe.isActive()) {
                            allProbesLive = false;
                            break;
                        }
                    }
                    for (Dot11Probe probe : nzyme.getProbes()) {
                        if (probe instanceof Dot11MonitorProbe) {
                            firstMonitor = (Dot11MonitorProbe) probe;
                            break;
                        }
                    }

                    if (firstMonitor != null) {
                        switch (firstMonitor.getChannelDesignator().getStatus()) {
                            case LOCKED:
                                labelDesignator.setText("  LOCK  ");
                                labelDesignator.setForegroundColor(TextColor.ANSI.BLACK);
                                labelDesignator.setBackgroundColor(TextColor.ANSI.GREEN);
                                break;
                            case UNLOCKED:
                                labelDesignator.setText("UNLOCKED");
                                labelDesignator.setForegroundColor(TextColor.ANSI.WHITE);
                                labelDesignator.setBackgroundColor(TextColor.ANSI.BLACK);
                                break;
                            case SWEEPING:
                                labelDesignator.setText("SWEEPING");
                                labelDesignator.setForegroundColor(TextColor.ANSI.BLACK);
                                labelDesignator.setBackgroundColor(TextColor.ANSI.YELLOW);
                                break;
                        }
                    }

                    if (allProbesLive) {
                        labelWiFiStatus.setText(" ONLINE");
                        labelWiFiStatus.setForegroundColor(TextColor.ANSI.GREEN);
                    } else {
                        labelWiFiStatus.setText("  WARN");
                        labelWiFiStatus.setForegroundColor(TextColor.ANSI.RED);
                    }

                    if (nzyme.getGroundStation().getTrackerDevice().isHealthy()) {
                        labelTrackerStatus.setText("  ONLINE");
                        labelTrackerStatus.setForegroundColor(TextColor.ANSI.GREEN);
                    } else {
                        labelTrackerStatus.setText("   WARN");
                        labelTrackerStatus.setForegroundColor(TextColor.ANSI.RED);
                    }

                    if (!nzyme.getBanditManager().isCurrentlyTracking()) {
                        labelTask.setText(" NONE ");
                        labelTask.setForegroundColor(TextColor.ANSI.BLACK);
                        labelTask.setBackgroundColor(TextColor.ANSI.WHITE);

                        labelBandit.setText("     NONE");
                        labelBandit.setForegroundColor(TextColor.ANSI.WHITE);

                        labelTrack.setText("  N/A  ");
                        labelTrack.setForegroundColor(TextColor.ANSI.WHITE);
                        labelTrack.setBackgroundColor(TextColor.ANSI.BLACK);

                        labelTrackSignal.setText("    N/A");
                        labelTrackSignal.setForegroundColor(TextColor.ANSI.WHITE);

                        labelTrackContact.setText("     N/A");
                        labelTrackContact.setForegroundColor(TextColor.ANSI.WHITE);

                        labelTrackFrames.setText("  N/A   ");
                        labelTrackFrames.setForegroundColor(TextColor.ANSI.WHITE);
                    } else {
                        // Currently tracking a bandit.
                        labelTask.setText(" TRCK ");
                        labelTask.setForegroundColor(TextColor.ANSI.BLACK);
                        labelTask.setBackgroundColor(TextColor.ANSI.GREEN);

                        String banditId = nzyme.getBanditManager().getCurrentlyTrackedBandit().uuid().toString().substring(0,6);
                        labelBandit.setText(getCenterPadding(banditId, 14) + banditId);
                        labelBandit.setForegroundColor(TextColor.ANSI.GREEN);

                        if (nzyme.getBanditManager().hasActiveTrack()) {
                            labelTrack.setText("  ACT  ");
                            labelTrack.setForegroundColor(TextColor.ANSI.BLACK);
                            labelTrack.setBackgroundColor(TextColor.ANSI.GREEN);

                            TrackerTrackSummary track = nzyme.getBanditManager().getTrackSummary();
                            if (track != null) {
                                String sig = String.valueOf(track.lastSignal());
                                labelTrackSignal.setText(getCenterPadding(sig, 10) + sig);
                                labelTrackSignal.setForegroundColor(TextColor.ANSI.GREEN);

                                labelTrackContact.setText("   " + track.lastContact().toString(DateTimeFormat.forPattern("HH:mm:ss")));
                                labelTrackContact.setForegroundColor(TextColor.ANSI.WHITE);

                                if (track.frameCount() > 99_999_999) {
                                    labelTrackFrames.setText("  >100M ");
                                } else {
                                    labelTrackFrames.setText(getCenterPadding(String.valueOf(track.frameCount()), 8) + track.frameCount());
                                }
                                labelTrackFrames.setForegroundColor(TextColor.ANSI.GREEN);
                            }
                        } else {
                            labelTrack.setText("  N/A  ");
                            labelTrack.setForegroundColor(TextColor.ANSI.BLACK);
                            labelTrack.setBackgroundColor(TextColor.ANSI.RED);
                            labelTrackSignal.setText("    N/A");
                            labelTrackSignal.setForegroundColor(TextColor.ANSI.WHITE);
                            labelTrackContact.setText("     N/A");
                            labelTrackContact.setForegroundColor(TextColor.ANSI.WHITE);
                            labelTrackFrames.setText("  N/A   ");
                            labelTrackFrames.setForegroundColor(TextColor.ANSI.WHITE);
                        }
                    }
                }, 0, 1, TimeUnit.SECONDS);
    }

    private void initializeGUI() throws IOException {
        Terminal terminal = new DefaultTerminalFactory()
                .setInitialTerminalSize(new TerminalSize(COLS,ROWS))
                .createTerminal();

        Screen screen = new TerminalScreen(terminal);
        screen.startScreen();

        BasicWindow window = new BasicWindow();
        Panel mainPanel = new Panel();

        // Top status.
        Panel statusPanel = new Panel(new GridLayout(8));
        statusPanel.addComponent(labelConnection.withBorder(Borders.singleLine("CONN")));
        statusPanel.addComponent(labelSignal.withBorder(Borders.singleLine("SIG")));
        statusPanel.addComponent(labelTrackerStatus.withBorder(Borders.singleLine("LINK DVC")));
        statusPanel.addComponent(labelWiFiStatus.withBorder(Borders.singleLine("802.11")));
        statusPanel.addComponent(labelWiFiChannels.withBorder(Borders.singleLine("CHANNEL")));
        statusPanel.addComponent(labelDesignator.withBorder(Borders.singleLine("DSGNTR")));
        statusPanel.addComponent(new EmptySpace(new TerminalSize(3, 3)));
        statusPanel.addComponent(labelTime.withBorder(Borders.singleLine("CLOCK")));
        mainPanel.addComponent(statusPanel);

        // Task.
        Panel taskPanel = new Panel(new GridLayout(6));
        taskPanel.addComponent(labelTask.withBorder(Borders.singleLine("TASK")));
        taskPanel.addComponent(labelBandit.withBorder(Borders.singleLine("BANDIT TARGET")));
        taskPanel.addComponent(labelTrack.withBorder(Borders.singleLine("TRACK")));
        taskPanel.addComponent(labelTrackSignal.withBorder(Borders.singleLine("TRACK SIG")));
        taskPanel.addComponent(labelTrackFrames.withBorder(Borders.singleLine("FRAMES")));
        taskPanel.addComponent(labelTrackContact.withBorder(Borders.singleLine("LAST CONTACT")));
        mainPanel.addComponent(taskPanel);

        // Events
        Panel eventsPanel = new Panel(new GridLayout(1));
        eventsPanel.addComponent(eventsTable.withBorder(Borders.singleLine("EVENTS")));
        eventsTable.setCellSelection(false);
        eventsTable.setSelectedRow(-1);
        eventsTable.setVisibleRows(5);
        eventsTable.setSize(new TerminalSize(COLS, 8));
        event(nzyme.getNodeID(), "Initialized tracker.");
        mainPanel.addComponent(eventsPanel);

        window.setComponent(mainPanel);
        window.setTheme(SimpleTheme.makeTheme(
                true,
                TextColor.ANSI.WHITE,
                TextColor.ANSI.BLACK,
                TextColor.ANSI.BLACK,
                TextColor.ANSI.BLUE,
                TextColor.ANSI.WHITE,
                TextColor.ANSI.RED,
                TextColor.ANSI.RED
        ));
        window.setHints(Arrays.asList(Window.Hint.CENTERED, Window.Hint.NO_DECORATIONS));

        MultiWindowTextGUI gui = new MultiWindowTextGUI(screen, new DefaultWindowManager(), new EmptySpace(TextColor.ANSI.BLACK));
        gui.addWindowAndWait(window);
    }

    @Override
    public void onConnectionStateChange(List<TrackerState> connectionState) {
        if (connectionState.contains(TrackerState.ONLINE)) {
            // We are online.
            labelConnection.setText("ONLINE");
            labelConnection.setForegroundColor(TextColor.ANSI.GREEN);
        } else {
            // We are offline.
            labelConnection.setText(" DARK");
            labelConnection.setForegroundColor(TextColor.ANSI.RED);

            labelSignal.setText(" n/a");
            labelSignal.setForegroundColor(TextColor.ANSI.RED);
        }
    }

    @Override
    public void onPingFromLeaderReceived(TrackerMessage.Ping ping, int rssi) {
        long percent = Math.round(rssi/255.0*100);
        if (percent == 100) {
            percent = 99;
        }

        TextColor color;

        if (percent >= 75) {
            color = TextColor.ANSI.GREEN;
        } else if (percent > 50){
            color = TextColor.ANSI.YELLOW;
        } else {
            color = TextColor.ANSI.RED;
        }

        labelSignal.setText(" " + percent + "%");
        labelSignal.setForegroundColor(color);
    }

    @Override
    public void onPingFromTrackerReceived(TrackerMessage.Ping ping, int rssi) {
        // no-op
    }

    @Override
    public void onStartTrackingRequestReceived(TrackerMessage.StartTrackRequest request) {
        event(request.getSource(), "Received request to track bandit.");
    }

    @Override
    public void onCancelTrackingRequestReceived(TrackerMessage.CancelTrackRequest request) {
        event(request.getSource(), "All bandit tracking canceled on leader request.");
    }

    @Override
    public void onInitialContactWithTrackedBandit(Bandit bandit) {
        // no-op
    }

    @Override
    public void onBanditTrace(Bandit bandit, int rssi) {
        // no-op
    }

    @Override
    public void onChannelSwitch(int previousChannel, int newChannel) {
        // no-op
    }

    private String wifiChannels() {
        List<Integer> channels = Lists.newArrayList();
        for (Dot11Probe probe : nzyme.getProbes()) {
            channels.add(probe.getCurrentChannel());
        }

        String s = Joiner.on(",").join(channels);

        return getCenterPadding(s, 9) + s;
    }

    private static String getCenterPadding(String string, int width) {
        StringBuilder padding = new StringBuilder();
        for (int i = 0; i < (width-string.length())/2; i++) {
            padding.append(" ");
        }

        return padding.toString();
    }

    private static String getRightPadding(String string, int width) {
        StringBuilder padding = new StringBuilder();
        for (int i = 0; i < (width-string.length()); i++) {
            padding.append(" ");
        }

        return padding.toString();
    }

    private void event(String source, String message) {
        //noinspection UnstableApiUsage
        events.add(Event.create(DateTime.now(), source, message));

        // Re-draw table.
        ArrayList<Event> list = Lists.newArrayList(this.events);
        Collections.reverse(list);

        eventsTable.getTableModel().clear();
        for (Event event : list) {
            String paddedSource = event.source().length() > 15 ? event.source().substring(0, 15) : event.source();
            String paddedMessage = event.message().length() > 55 ? event.message().substring(0, 55) : event.message();
            eventsTable.getTableModel().addRow(
                    event.timestamp().toString(DateTimeFormat.forPattern("HH:mm:ss")),
                    paddedSource + getRightPadding(paddedSource, 15),
                    paddedMessage + getRightPadding(paddedMessage, 55)
            );
        }
    }

    @AutoValue
    public static abstract class Event {

        public abstract DateTime timestamp();
        public abstract String source();
        public abstract String message();

        public static Event create(DateTime timestamp, String source, String message) {
            return builder()
                    .timestamp(timestamp)
                    .source(source)
                    .message(message)
                    .build();
        }

        public static Builder builder() {
            return new AutoValue_TextGUIHID_Event.Builder();
        }

        @AutoValue.Builder
        public abstract static class Builder {
            public abstract Builder timestamp(DateTime timestamp);

            public abstract Builder source(String source);

            public abstract Builder message(String message);

            public abstract Event build();
        }

    }

}

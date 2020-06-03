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

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;
import horse.wtf.nzyme.bandits.Bandit;
import horse.wtf.nzyme.bandits.trackers.TrackerState;
import horse.wtf.nzyme.bandits.trackers.protobuf.TrackerMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executors;

public class TextGUIHID implements TrackerHID {

    private static final Logger LOG = LogManager.getLogger(TextGUIHID.class);

    private final Label labelConnection = new Label(" DARK").setForegroundColor(TextColor.ANSI.RED);
    private final Label labelSignal = new Label("???").setForegroundColor(TextColor.ANSI.RED);
    private final Label labelSync = new Label("MISS").setForegroundColor(TextColor.ANSI.RED);

    @Override
    public void initialize() {
        Executors.newSingleThreadExecutor(new ThreadFactoryBuilder()
                .setDaemon(true)
                .setNameFormat("textguihid-%d").build())
                .submit(() -> {
                    try {
                        initializeGUI();
                    } catch (Exception e) {
                        LOG.error("Could not initialize Text GUI HID.", e);
                    }
                });
    }

    private void initializeGUI() throws IOException {
        Terminal terminal = new DefaultTerminalFactory()
                .setInitialTerminalSize(new TerminalSize(80,22))
                .createTerminal();

        Screen screen = new TerminalScreen(terminal);
        screen.startScreen();

        BasicWindow window = new BasicWindow();
        Panel mainPanel = new Panel();

        Panel statusPanel = new Panel(new LinearLayout(Direction.HORIZONTAL));
        statusPanel.addComponent(labelConnection.withBorder(Borders.singleLine("CONN")));
        statusPanel.addComponent(labelSignal.withBorder(Borders.singleLine("SIG")));
        statusPanel.addComponent(labelSync.withBorder(Borders.singleLine("SYNC")));
        mainPanel.addComponent(statusPanel);

        window.setComponent(mainPanel);

        MultiWindowTextGUI gui = new MultiWindowTextGUI(screen, new DefaultWindowManager(), new EmptySpace(TextColor.ANSI.BLUE));
        gui.addWindowAndWait(window);
    }

    @Override
    public void onConnectionStateChange(List<TrackerState> connectionState) {
        if (connectionState.contains(TrackerState.ONLINE)) {
            labelConnection.setText("ONLINE");
            labelConnection.setForegroundColor(TextColor.ANSI.GREEN);

            if (connectionState.contains(TrackerState.OUT_OF_SYNC)) {
                labelSync.setText("MISS");
                labelSync.setForegroundColor(TextColor.ANSI.RED);
            } else {
                labelSync.setText("SYNC");
                labelSync.setForegroundColor(TextColor.ANSI.GREEN);
            }
        } else {
            labelConnection.setText(" DARK");
            labelConnection.setForegroundColor(TextColor.ANSI.RED);
            labelSync.setText("UNCL");
            labelSync.setForegroundColor(TextColor.ANSI.YELLOW);
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

    }

    @Override
    public void onBanditReceived(TrackerMessage.BanditBroadcast bandit) {

    }

    @Override
    public void onStartTrackingRequestReceived(TrackerMessage.StartTrackRequest request) {

    }

    @Override
    public void onCancelTrackingRequestReceived(TrackerMessage.CancelTrackRequest request) {

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
    public void onContactRequestReceived() {

    }

}

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

package app.nzyme.core.configuration;

import com.beust.jcommander.internal.Lists;
import com.google.common.collect.ImmutableList;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
import app.nzyme.core.dot11.networks.signalstrength.tracks.TrackDetector;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class BaseDot11ConfigurationLoader {

    private static final Logger LOG = LogManager.getLogger(BaseDot11ConfigurationLoader.class);

    private final Config root;

    public BaseDot11ConfigurationLoader(Config root) {
        this.root = root;
    }

    public ImmutableList<Dot11MonitorDefinition> parseDot11Monitors() {
        ImmutableList.Builder<Dot11MonitorDefinition> result = new ImmutableList.Builder<>();

        for (Config config : root.getConfigList(ConfigurationKeys.DOT11_MONITORS)) {
            if (!Dot11MonitorDefinition.checkConfig(config)) {
                LOG.info("Skipping 802.11 monitor with invalid configuration. Invalid monitor: [{}]", config);
                continue;
            }

            final boolean skipEnableMonitor;
            if (config.hasPath(ConfigurationKeys.SKIP_ENABLE_MONITOR)) {
                skipEnableMonitor = config.getBoolean(ConfigurationKeys.SKIP_ENABLE_MONITOR);
            } else {
                skipEnableMonitor = false;
            }

            final int maxIdleTimeSeconds;
            if (config.hasPath(ConfigurationKeys.MAX_IDLE_TIME_SECONDS)) {
                maxIdleTimeSeconds = config.getInt(ConfigurationKeys.MAX_IDLE_TIME_SECONDS);
            } else {
                maxIdleTimeSeconds = 60;
            }

            result.add(Dot11MonitorDefinition.create(
                    config.getString(ConfigurationKeys.DEVICE),
                    ImmutableList.copyOf(config.getIntList(ConfigurationKeys.CHANNELS)),
                    config.getString(ConfigurationKeys.HOP_COMMAND),
                    config.getInt(ConfigurationKeys.HOP_INTERVAL),
                    skipEnableMonitor,
                    maxIdleTimeSeconds
            ));
        }

        return result.build();
    }

    public ImmutableList<Dot11NetworkDefinition> parseDot11Networks() {
        ImmutableList.Builder<Dot11NetworkDefinition> result = new ImmutableList.Builder<>();

        for (Config config : root.getConfigList(ConfigurationKeys.DOT11_NETWORKS)) {
            if (!Dot11NetworkDefinition.checkConfig(config)) {
                LOG.info("Skipping 802.11 network with invalid configuration. Invalid network: [{}]", config);
                continue;
            }

            ImmutableList.Builder<Dot11BSSIDDefinition> lowercaseBSSIDs = new ImmutableList.Builder<>();
            for (Config bssid : config.getConfigList(ConfigurationKeys.BSSIDS)) {
                TrackDetector.TrackDetectorConfig trackDetectorConfig = null;
                if (bssid.hasPath(ConfigurationKeys.TRACK_DETECTOR)) {
                    Config tdc = bssid.getConfig(ConfigurationKeys.TRACK_DETECTOR);
                    if (tdc.hasPath(ConfigurationKeys.FRAME_THRESHOLD) && tdc.hasPath(ConfigurationKeys.GAP_THRESHOLD) && tdc.hasPath(ConfigurationKeys.SIGNAL_CENTERLINE_JITTER)) {
                        trackDetectorConfig = TrackDetector.TrackDetectorConfig.create(
                                tdc.getInt(ConfigurationKeys.FRAME_THRESHOLD),
                                tdc.getInt(ConfigurationKeys.GAP_THRESHOLD),
                                tdc.getInt(ConfigurationKeys.SIGNAL_CENTERLINE_JITTER)
                        );
                    } else {
                        LOG.warn("Not parsing track detector config. Missing at least one configuration key. Please consult documentation.");
                    }
                }

                lowercaseBSSIDs.add(Dot11BSSIDDefinition.create(
                        bssid.getString(ConfigurationKeys.ADDRESS).toLowerCase(),
                        bssid.getStringList(ConfigurationKeys.FINGERPRINTS),
                        trackDetectorConfig
                ));
            }

            result.add(Dot11NetworkDefinition.create(
                    config.getString(ConfigurationKeys.SSID),
                    lowercaseBSSIDs.build(),
                    config.getIntList(ConfigurationKeys.CHANNELS),
                    config.getStringList(ConfigurationKeys.SECURITY),
                    config.getInt(ConfigurationKeys.BEACON_RATE)
            ));
        }

        return result.build();
    }

    public void validate() throws InvalidConfigurationException, IncompleteConfigurationException {
        validateMonitors();
        validateNetworks();
    }

    public void validateMonitors() throws IncompleteConfigurationException, InvalidConfigurationException {
        // 802.11 Monitors.
        int i = 0;
        for (Config c : root.getConfigList(ConfigurationKeys.DOT11_MONITORS)) {
            String where = ConfigurationKeys.DOT11_MONITORS + "." + "#" + i;
            ConfigurationValidator.expect(c, ConfigurationKeys.DEVICE, where, String.class);
            ConfigurationValidator.expect(c, ConfigurationKeys.CHANNELS, where, ImmutableList.class);
            ConfigurationValidator.expect(c, ConfigurationKeys.HOP_COMMAND, where, String.class);
            ConfigurationValidator.expect(c, ConfigurationKeys.HOP_INTERVAL, where, Integer.class);
            i++;
        }

        // All channels are all integers, larger than 0.
        validateChannelList(ConfigurationKeys.DOT11_MONITORS);

        // 802_11 monitors: No channel is used in any other monitor.
        List<Integer> usedChannels = Lists.newArrayList();
        for (Dot11MonitorDefinition monitor : parseDot11Monitors()) {
            for (Integer channel : monitor.channels()) {
                if (usedChannels.contains(channel)) {
                    throw new InvalidConfigurationException("Channel [" + channel + "] is defined for multiple 802.11 monitors. You should not have multiple monitors tuned to the same channel.");
                }
            }
            usedChannels.addAll(monitor.channels());
        }

        // 802_11 monitors: Device is not used in any other configuration.
        List<String> devices = Lists.newArrayList();
        for (Dot11MonitorDefinition monitor : parseDot11Monitors()) {
            if (devices.contains(monitor.device())) {
                throw new InvalidConfigurationException("Device [" + monitor.device() + "] is defined for multiple 802.11 monitors. You should not have multiple monitors using the same device.");
            }
            devices.add(monitor.device());
        }
    }

    public void validateNetworks() throws IncompleteConfigurationException, InvalidConfigurationException {
        // 802_11 networks: SSID is unique.
        List<String> ssids = Lists.newArrayList();
        for (Dot11NetworkDefinition net : parseDot11Networks()) {
            if (ssids.contains(net.ssid())) {
                throw new InvalidConfigurationException("SSID [" + net.ssid() + "] is defined multiple times. You cannot define a network with the same SSID more than once.");
            }
            ssids.add(net.ssid());
        }

        // 802.11 networks: BSSIDs are unique for this network. (note that a BSSID can be used in multiple networks)
        for (Dot11NetworkDefinition net : parseDot11Networks()) {
            List<String> bssids = Lists.newArrayList();
            for (Dot11BSSIDDefinition bssid : net.bssids()) {
                if(bssids.contains(bssid.address())) {
                    throw new InvalidConfigurationException("Network [" + net.ssid() + "] has at least one BSSID defined twice. You cannot define a BSSID for the same network more than once.");
                }
                bssids.add(bssid.address());
            }
        }
    }

    private void validateChannelList(String key) throws InvalidConfigurationException {
        int x = 0;
        List<Integer> usedChannels = Lists.newArrayList();
        for (Config c : root.getConfigList(key)) {
            String where = key + "." + "#" + x;
            try {
                for (Integer channel : c.getIntList(ConfigurationKeys.CHANNELS)) {
                    if (channel < 1) {
                        throw new InvalidConfigurationException("Invalid channels in list for [" + where + "}. All channels must be integers larger than 0.");
                    }

                    if (usedChannels.contains(channel)) {
                        throw new InvalidConfigurationException("Duplicate channel <" + channel + "> in list for [ " + where + " ]. Channels cannot be duplicate per monitor or across multiple monitors.");
                    }

                    usedChannels.add(channel);
                }
            } catch(ConfigException e) {
                LOG.error(e);
                throw new InvalidConfigurationException("Invalid channels list for [" + where + "}. All channels must be integers larger than 0.");
            } finally {
                x++;
            }
        }
    }

}

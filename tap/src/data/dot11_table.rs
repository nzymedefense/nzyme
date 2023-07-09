use std::{collections::HashMap, sync::Mutex};

use log::error;

use crate::{
    dot11::frames::{Dot11BeaconFrame, SecurityInformation, FrameSubType, InfraStructureType, Dot11DataFrame, Dot11DataFrameDirection, Dot11ProbeRequestFrame},
    link::payloads::{
        AdvertisedNetworkReport, BssidReport, Dot11CipherSuites, Dot11TableReport,
        SecurityInformationReport, SignalStrengthReport, Dot11ChannelStatisticsReport, Dot11ClientStatisticsReport, Dot11ClientReport,
    }, helpers::network::is_mac_address_multicast,
};

#[derive(Debug)]
pub struct Dot11Table {
    pub bssids: Mutex<HashMap<String, Bssid>>,
    pub clients: Mutex<HashMap<String, Client>>
}

#[derive(Debug)]
pub struct Bssid {
    pub advertised_networks: HashMap<String, AdvertisedNetwork>,
    pub clients: HashMap<String, Dot11ClientStatistics>,
    pub hidden_ssid_frames: u128,
    pub signal_strengths: Vec<i8>,
    pub fingerprints: Vec<String>,
}

#[derive(Debug)]
pub struct Client {
    pub probe_request_ssids: Vec<String>,
    pub wildcard_probe_requests: u128
    // TODO add signal strength like BSSID
}

#[derive(Debug)]
pub struct Dot11ChannelStatistics {
    frames: u128,
    bytes: u128
}

#[derive(Debug)]
pub struct Dot11ClientStatistics {
    tx_frames: u128,
    tx_bytes: u128,
    rx_frames: u128,
    rx_bytes: u128
}

#[derive(Debug)]
pub struct AdvertisedNetwork {
    pub security: Vec<SecurityInformation>,
    pub beacon_advertisements: u128,
    pub proberesp_advertisements: u128,
    pub fingerprints: Vec<String>,
    pub rates: Vec<f32>,
    pub wps: bool,
    pub signal_strengths: HashMap<u16, Vec<i8>>,
    pub infrastructure_types: Vec<InfraStructureType>,
    pub channel_statistics: HashMap<u16, HashMap<FrameSubType, Dot11ChannelStatistics>>
}

impl Dot11Table {
    pub fn new() -> Self {
        Self {
            bssids: Mutex::new(HashMap::new()),
            clients: Mutex::new(HashMap::new())
        }
    }

    pub fn register_beacon_frame(&mut self, beacon: Dot11BeaconFrame) {
        let signal_strength: i8 = match beacon.header.antenna_signal {
            Some(s) => s,
            None => 0,
        };

        match self.bssids.lock() {
            Ok(mut bssids) => {
                match bssids.get_mut(&beacon.transmitter) {
                    Some(bssid) => {
                        // Update existing SSIDs of BSSID.
                        match beacon.ssid {
                            Some(ssid) => {
                                // Has this BSSID advertised this SSID before?
                                match bssid.advertised_networks.get_mut(&ssid) {
                                    Some(ssid) => {
                                        // Update existing SSID.
                                        ssid.security = beacon.security;
                                        ssid.wps = beacon.has_wps;

                                        if !ssid.fingerprints.contains(&beacon.fingerprint) {
                                            ssid.fingerprints.push(beacon.fingerprint.clone());
                                        }

                                        if let Some(rates) = &beacon.tagged_parameters.supported_rates {
                                            Self::update_existing_ssid_rates(ssid, &rates);
                                        }

                                        if let Some(rates) = &beacon.tagged_parameters.extended_supported_rates {
                                            Self::update_existing_ssid_rates(ssid, &rates);
                                        }

                                        if !ssid.infrastructure_types.contains(&beacon.capabilities.infrastructure_type) {
                                            ssid.infrastructure_types.push(beacon.capabilities.infrastructure_type);
                                        }

                                        Self::update_existing_channel_statistics(
                                            FrameSubType::Beacon, beacon.header.frequency, beacon.length, ssid
                                        );

                                        if let Some(freq) = &beacon.header.frequency {
                                            match ssid.signal_strengths.get_mut(freq) {
                                                Some(ss) => ss.push(signal_strength),
                                                None => {
                                                    ssid.signal_strengths.insert(*freq, vec![signal_strength]);
                                                },
                                            }
                                        }

                                        ssid.beacon_advertisements += 1;
                                    }
                                    None => {
                                        // Insert new SSID.
                                        bssid.advertised_networks.insert(
                                            ssid,
                                            AdvertisedNetwork {
                                                security: beacon.security,
                                                fingerprints: vec![beacon.fingerprint.clone()],
                                                beacon_advertisements: 1,
                                                proberesp_advertisements: 0,
                                                rates: Self::build_initial_ssid_rates(
                                                    &beacon.tagged_parameters.supported_rates,
                                                    &beacon.tagged_parameters.extended_supported_rates
                                                ),
                                                wps: beacon.has_wps,
                                                signal_strengths: Self::build_initial_signal_strengths(&beacon.header.frequency, signal_strength),
                                                infrastructure_types: vec![beacon.capabilities.infrastructure_type],
                                                channel_statistics: Self::build_initial_channel_statistics(
                                                    FrameSubType::Beacon, &beacon.header.frequency, beacon.length
                                                )
                                            },
                                        );
                                    }
                                }
                            }
                            None => {
                                // No SSID.
                                bssid.hidden_ssid_frames += 1;
                            }
                        }

                        // Update BSSID.
                        if !bssid.fingerprints.contains(&beacon.fingerprint) {
                            bssid.fingerprints.push(beacon.fingerprint);
                        }
                        bssid.signal_strengths.push(signal_strength);
                    }
                    None => {
                        // BSSID not yet in table.
                        let (advertised_networks, hidden_ssid_frames) = match beacon.ssid {
                            Some(ssid) => (
                                HashMap::from([(
                                    ssid,
                                    AdvertisedNetwork {
                                        security: beacon.security,
                                        fingerprints: vec![beacon.fingerprint.clone()],
                                        beacon_advertisements: 1,
                                        proberesp_advertisements: 0,
                                        rates: Self::build_initial_ssid_rates(
                                            &beacon.tagged_parameters.supported_rates,
                                            &beacon.tagged_parameters.extended_supported_rates
                                        ),
                                        wps: beacon.has_wps,
                                        signal_strengths: Self::build_initial_signal_strengths(
                                            &beacon.header.frequency, signal_strength
                                        ),
                                        infrastructure_types: vec![beacon.capabilities.infrastructure_type],
                                        channel_statistics: Self::build_initial_channel_statistics(
                                            FrameSubType::Beacon, &beacon.header.frequency, beacon.length
                                        )
                                    },
                                )]),
                                0,
                            ),
                            None => (HashMap::new(), 1),
                        };

                        bssids.insert(
                            beacon.transmitter,
                            Bssid {
                                advertised_networks,
                                hidden_ssid_frames,
                                clients: HashMap::new(),
                                signal_strengths: vec![signal_strength],
                                fingerprints: vec![beacon.fingerprint]
                            },
                        );
                    }
                };
            }
            Err(e) => error!("Could not acqure BSSIDs table mutex: {}", e),
        }
    }

    pub fn register_probe_request_frame(&self, frame: Dot11ProbeRequestFrame) {
        match self.clients.lock() {
            Ok(mut clients) => {
                match clients.get_mut(&frame.transmitter) {
                    Some(client) => {
                        // Update existing client.
                        match frame.ssid {
                            Some(ssid) => {
                                // Specific/SSID request.
                                if !client.probe_request_ssids.contains(&ssid) {
                                    client.probe_request_ssids.push(ssid);
                                }
                            },
                            None => {
                                // Wildcard request.
                                client.wildcard_probe_requests += 1;
                            }
                        }
                    },
                    None => {
                        // First time we are seeing this client.
                        let (probe_request_ssids, wildcard_probe_requests) = match frame.ssid {
                            Some(ssid) => (vec![ssid], 0),
                            None => (Vec::new(), 1),
                        };

                        clients.insert(
                            frame.transmitter,
                            Client {
                                probe_request_ssids,
                                wildcard_probe_requests
                            }
                        );
                    }
                }
            },
            Err(e) => error!("Could not acqure clients table mutex: {}", e)
        }
    }

    pub fn register_data_frame(&self, frame: Dot11DataFrame) {
        let (sta, tx_frames, tx_bytes, rx_frames, rx_bytes) = match frame.ds.direction {
            Dot11DataFrameDirection::Entering => {
                (frame.ds.destination, 0, 0, 1, frame.length)
            },
            Dot11DataFrameDirection::Leaving => {
                (frame.ds.destination, 1, frame.length, 0, 0)
            },
            Dot11DataFrameDirection::NotLeavingOrAdHoc |
            Dot11DataFrameDirection::WDS => return
        };

        if frame.ds.direction == Dot11DataFrameDirection::Entering && sta == "FF:FF:FF:FF:FF:FF" {
            // Ignore entering franes with wildcard destination.
            return
        }

        if is_mac_address_multicast(&sta) {
            // Not interested in multicast frames.
            return
        }

        match self.bssids.lock() {
            Ok(mut bssids) => {
                match bssids.get_mut(&frame.ds.bssid) {
                    Some(bssid) => {
                        match bssid.clients.get_mut(&sta) {
                            Some(client) => {
                                // Update existing client.
                                client.tx_frames += tx_frames;
                                client.tx_bytes += tx_bytes as u128;
                                client.rx_frames += rx_frames;
                                client.rx_bytes += rx_bytes as u128;
                            },
                            None => {
                                // First time seeing this client.
                                bssid.clients.insert(sta, Dot11ClientStatistics {
                                    tx_frames,
                                    tx_bytes: tx_bytes as u128,
                                    rx_frames,
                                    rx_bytes: rx_bytes as u128
                                });
                            },
                        }
                    },
                    None => {/* Ignore data frames for so far unknown BSSIDs. */},
                }
            },
            Err(e) => error!("Could not acqure BSSIDs table mutex: {}", e),
        }
    }

    pub fn build_initial_channel_statistics(frame_subtype: FrameSubType, frequency: &Option<u16>, frame_length: usize) 
            -> HashMap<u16, HashMap<FrameSubType, Dot11ChannelStatistics>> {
        let mut channel_statistics = HashMap::new();
        if let Some(freq) = frequency {
            let mut stats = HashMap::new();
            stats.insert(
                frame_subtype,
                Dot11ChannelStatistics {
                    frames: 1,
                    bytes: frame_length as u128
                }
            );

            channel_statistics.insert(*freq, stats);
        }

        channel_statistics
    }

    pub fn update_existing_channel_statistics(frame_subtype: FrameSubType, frequency: Option<u16>, frame_length: usize,
            ssid: &mut AdvertisedNetwork) {
        if let Some(freq) = frequency {
            match ssid.channel_statistics.get_mut(&freq) {
                Some(types) => {
                    match types.get_mut(&frame_subtype) {
                        Some(stats) => {
                            // Add frame to stats.
                            stats.frames += 1;
                            stats.bytes += frame_length as u128;
                        },
                        None => {
                            // First frame of this subtype on this frequency.
                            types.insert(frame_subtype, Dot11ChannelStatistics {
                                frames: 1,
                                bytes: frame_length as u128
                            });
                        }
                    }
                },
                None => {
                    // First frame for this frequency. 
                    let mut stats = HashMap::new();
                    stats.insert(frame_subtype, Dot11ChannelStatistics {
                        frames: 1,
                        bytes: frame_length as u128
                    });
                    ssid.channel_statistics.insert(freq, stats);
                }
            }
        }
    }

    pub fn build_initial_ssid_rates(rates: &Option<Vec<f32>>, extended_rates: &Option<Vec<f32>>) -> Vec<f32> {
        let mut result = Vec::new();
        if let Some(rates) = rates {
            for rate in rates {
                result.push(rate.clone())
            }
        }

        if let Some(rates) = extended_rates {
            for rate in rates {
                result.push(rate.clone())
            }
        }

        result
    }

    pub fn update_existing_ssid_rates(ssid: &mut AdvertisedNetwork, rates: &Vec<f32>) {
        for rate in rates {
            if !ssid.rates.contains(rate) {
                ssid.rates.push(rate.clone());
            }
        }
    }
    pub fn build_initial_signal_strengths(channel: &Option<u16>, signal_strength: i8) -> HashMap<u16, Vec<i8>> {
        let mut map = HashMap::new();

        match channel {
            Some(channel) => map.insert(*channel, vec![signal_strength]),
            None => return map,
        };

        map
    }

    pub fn clear_ephemeral(&mut self) {
        match self.bssids.lock() {
            Ok(mut bssids) => bssids.clear(),
            Err(e) => error!("Could not acquire BSSIDs table mutex: {}", e)
        }

        match self.clients.lock() {
            Ok(mut clients) => clients.clear(),
            Err(e) => error!("Could not acquire clients table mutex: {}", e)
        }
    }

    pub fn to_report(&self) -> Dot11TableReport {
        let mut bssid_report: HashMap<String, BssidReport> = HashMap::new();

        match self.bssids.lock() {
            Ok(bssids) => {
                for (bssid, info) in &*bssids {
                    let mut advertised_networks: HashMap<String, AdvertisedNetworkReport> =
                        HashMap::new();
                    for (ssid, netinfo) in &info.advertised_networks {
                        let mut netsec_report: Vec<SecurityInformationReport> = Vec::new();

                        for netsecinfo in &netinfo.security {
                            let mut protocols: Vec<String> = Vec::new();
                            for protocol in &netsecinfo.protocols {
                                protocols.push(protocol.to_string());
                            }

                            let suites: Dot11CipherSuites = match &netsecinfo.suites {
                                Some(suite) => {
                                    let mut pairwise_ciphers: Vec<String> = Vec::new();
                                    for cipher in &suite.pairwise_ciphers {
                                        pairwise_ciphers.push(cipher.to_string());
                                    }

                                    let mut key_management_modes: Vec<String> = Vec::new();
                                    for mode in &suite.key_management_modes {
                                        key_management_modes.push(mode.to_string());
                                    }

                                    Dot11CipherSuites {
                                        group_cipher: suite.group_cipher.to_string(),
                                        pairwise_ciphers,
                                        key_management_modes
                                    }
                                },
                                None => Dot11CipherSuites {
                                    group_cipher: "".to_string(),
                                    pairwise_ciphers: Vec::new(),
                                    key_management_modes: Vec::new(),
                                },
                            };

                            netsec_report.push(SecurityInformationReport { protocols, suites });
                        }

                        let mut channel_statistics = HashMap::new();
                        for (frequency, frame_types) in &netinfo.channel_statistics {
                            let mut frame_type_summary = HashMap::new();
                            for (frame_type, stats) in frame_types {
                                frame_type_summary.insert(
                                    frame_type.to_string().to_lowercase(),
                                    Dot11ChannelStatisticsReport {
                                        frames: stats.frames,
                                        bytes: stats.bytes
                                    });
                            }

                            channel_statistics.insert(*frequency, frame_type_summary);
                        }

                        let mut infrastructure_types: Vec<String> = Vec::new();
                        for t in &netinfo.infrastructure_types {
                            infrastructure_types.push(t.to_string());
                        }

                        advertised_networks.insert(
                            ssid.clone(),
                            AdvertisedNetworkReport {
                                security: netsec_report,
                                fingerprints: netinfo.fingerprints.clone(),
                                beacon_advertisements: netinfo.beacon_advertisements,
                                proberesp_advertisements: netinfo.proberesp_advertisements,
                                rates: netinfo.rates.clone(),
                                wps: netinfo.wps,
                                signal_strength: calculate_signal_strengh_report(
                                    &netinfo.signal_strengths,
                                ),
                                signal_histogram: calculate_signal_histogram(&netinfo.signal_strengths),
                                infrastructure_types,
                                channel_statistics
                            },
                        );
                    }

                    let mut clients = HashMap::new();
                    for (sta, stats) in &info.clients {
                        clients.insert(sta.clone(), Dot11ClientStatisticsReport {
                            tx_frames: stats.tx_frames,
                            tx_bytes: stats.tx_bytes,
                            rx_frames: stats.rx_frames,
                            rx_bytes: stats.rx_bytes,
                        });
                    }

                    bssid_report.insert(
                        bssid.clone(),
                        BssidReport {
                            advertised_networks,
                            clients,
                            hidden_ssid_frames: info.hidden_ssid_frames,
                            signal_strength: calculate_1d_signal_strengh_report(
                                &info.signal_strengths,
                            ),
                            fingerprints: info.fingerprints.clone(),
                        },
                    );
                }
            }
            Err(e) => error!("Could not acqure BSSIDs table mutex: {}", e),
        }

        let mut clients_report: HashMap<String, Dot11ClientReport> = HashMap::new();

        match self.clients.lock() {
            Ok(clients) => {
                for (client, info) in &*clients {
                    clients_report.insert(
                        client.clone(),
                        Dot11ClientReport {
                            probe_request_ssids: info.probe_request_ssids.clone(),
                            wildcard_probe_requests: info.wildcard_probe_requests
                        }
                    );
                }
            },
            Err(e) => error!("Could not acqure BSSIDs table mutex: {}", e),
        }

        Dot11TableReport {
            bssids: bssid_report,
            clients: clients_report
        }
    }
}

// yo this all below here needs some serious refactoring and dedup lmao

fn calculate_signal_strengh_report(signal_strengths: &HashMap<u16, Vec<i8>>) -> SignalStrengthReport {
    let mut sum: i128 = 0;
    let mut min = 0;
    let mut max = -100;

    for channel in signal_strengths.values() {
        for ss in channel {
            sum += *ss as i128;

            if ss > &max {
                max = *ss;
            }

            if ss < &min {
                min = *ss;
            }
        }
    }

    let count = signal_strengths.clone().len() as i128;
    let average = (sum / count) as f32;

    SignalStrengthReport {
        min,
        max,
        average,
    }
}

fn calculate_1d_signal_strengh_report(signal_strengths: &Vec<i8>) -> SignalStrengthReport {
    let mut sum: i128 = 0;
    let mut min = 0;
    let mut max = -100;
    for ss in signal_strengths {
        sum += *ss as i128;

        if ss > &max {
            max = *ss;
        }

        if ss < &min {
            min = *ss;
        }
    }

    let count = signal_strengths.clone().len() as i128;
    let average = (sum / count) as f32;

    SignalStrengthReport {
        min,
        max,
        average,
    }
}

fn calculate_signal_histogram(signal_strengths: &HashMap<u16, Vec<i8>>) -> HashMap<u16, HashMap<i8, u128>> {
    let mut histograms: HashMap<u16, HashMap<i8, u128>> = HashMap::new();

    for (channel, signals) in signal_strengths {
        match histograms.get_mut(channel) {
            Some(channel) => {
                // Update existing channel.
                for signal in signals {
                    match channel.get_mut(signal) {
                        Some(existing) => { *existing += 1; },
                        None => { channel.insert(*signal, 1); }
                    }
                }
            },
            None => {
                // First time channel.
                let mut histogram: HashMap<i8, u128> = HashMap::new();
                for signal in signals {
                    match histogram.get_mut(signal) {
                        Some(existing) => { *existing += 1; },
                        None => { histogram.insert(*signal, 1); }
                    }
                }
                histograms.insert(*channel, histogram);
            },
        }
    }

    histograms
}
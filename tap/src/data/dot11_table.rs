use std::{collections::HashMap, sync::Mutex};

use log::{error};

use crate::{
    dot11::frames::{Dot11BeaconFrame, SecurityInformation, FrameSubType, InfraStructureType},
    link::payloads::{
        AdvertisedNetworkReport, BssidReport, Dot11CipherSuites, Dot11TableReport,
        SecurityInformationReport, SignalStrengthReport, Dot11ChannelStatisticsReport,
    },
};

#[derive(Debug)]
pub struct Dot11Table {
    pub bssids: Mutex<HashMap<String, Bssid>>,
}

#[derive(Debug)]
pub struct Bssid {
    pub advertised_networks: HashMap<String, AdvertisedNetwork>,
    pub hidden_ssid_frames: u128,
    pub signal_strengths: Vec<i8>,
    pub fingerprints: Vec<String>,
}

#[derive(Debug)]
pub struct Dot11ChannelStatistics {
    frames: u128,
    bytes: u128
}

#[derive(Debug)]
pub struct AdvertisedNetwork {
    pub security: Vec<SecurityInformation>,
    pub fingerprints: Vec<String>,
    pub wps: bool,
    pub signal_strengths: Vec<i8>,
    pub infrastructure_types: Vec<InfraStructureType>,
    pub channel_statistics: HashMap<u16, HashMap<FrameSubType, Dot11ChannelStatistics>>
}

impl Dot11Table {
    pub fn new() -> Self {
        Self {
            bssids: Mutex::new(HashMap::new()),
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

                                        if !ssid.infrastructure_types.contains(&beacon.capabilities.infrastructure_type) {
                                            ssid.infrastructure_types.push(beacon.capabilities.infrastructure_type);
                                        }

                                        Self::update_existing_channel_statistics(
                                            FrameSubType::Beacon, beacon.header.frequency, beacon.length, ssid
                                        );
                                        ssid.signal_strengths.push(signal_strength);
                                    }
                                    None => {
                                        // Insert new SSID.
                                        bssid.advertised_networks.insert(
                                            ssid,
                                            AdvertisedNetwork {
                                                security: beacon.security,
                                                fingerprints: vec![beacon.fingerprint.clone()],
                                                wps: beacon.has_wps,
                                                signal_strengths: vec![signal_strength],
                                                infrastructure_types: vec![beacon.capabilities.infrastructure_type],
                                                channel_statistics: Self::build_initial_channel_statistics(
                                                    FrameSubType::Beacon, beacon.header.frequency, beacon.length
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
                                        wps: beacon.has_wps,
                                        signal_strengths: vec![signal_strength],
                                        infrastructure_types: vec![beacon.capabilities.infrastructure_type],
                                        channel_statistics: Self::build_initial_channel_statistics(
                                            FrameSubType::Beacon, beacon.header.frequency, beacon.length)
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

    pub fn build_initial_channel_statistics(frame_subtype: FrameSubType, frequency: Option<u16>, frame_length: usize) 
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

            channel_statistics.insert(freq, stats);
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
                }
            }
        }
    }

    pub fn clear_ephemeral(&mut self) {
        match self.bssids.lock() {
            Ok(mut bssids) => bssids.clear(),
            Err(e) => error!("Could not acquire BSSIDs table mutex: {}", e)
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
                                wps: netinfo.wps,
                                signal_strength: calculate_signal_strengh_report(
                                    &netinfo.signal_strengths,
                                ),
                                infrastructure_types,
                                channel_statistics
                            },
                        );
                    }

                    bssid_report.insert(
                        bssid.clone(),
                        BssidReport {
                            advertised_networks,
                            hidden_ssid_frames: info.hidden_ssid_frames,
                            signal_strength: calculate_signal_strengh_report(
                                &info.signal_strengths,
                            ),
                            fingerprints: info.fingerprints.clone(),
                        },
                    );
                }
            }
            Err(e) => error!("Could not acqure BSSIDs table mutex: {}", e),
        }

        Dot11TableReport {
            bssids: bssid_report,
        }
    }
}

fn calculate_signal_strengh_report(signal_strengths: &Vec<i8>) -> SignalStrengthReport {
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

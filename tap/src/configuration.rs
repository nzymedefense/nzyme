use std::collections::HashMap;
use std::env;
use std::fs::read_to_string;

use anyhow::{Result, bail, Error};
use log::info;
use regex::Regex;
use reqwest::Url;
use serde::Deserialize;

#[derive(Debug, Clone, Deserialize)]
pub struct Configuration {
    pub general: General,
    pub wifi_interfaces: Option<HashMap<String, WifiInterface>>,
    pub ethernet_interfaces: Option<HashMap<String, EthernetInterface>>,
    pub performance: Performance,
    pub protocols: Protocols,
    pub misc: Misc
}

#[derive(Debug, Clone, Deserialize)]
pub struct General {
    pub leader_secret: String,
    pub leader_uri: String,
    pub accept_insecure_certs: bool,
}

#[derive(Debug, Clone, Deserialize)]
pub struct EthernetInterface {
    pub active: bool
}

#[derive(Debug, Clone, Deserialize)]
pub struct WifiInterface {
    pub active: bool,
    pub disable_hopper: Option<bool>,
    pub channels_2g: Vec<u16>,
    pub channels_5g: Vec<u16>,
    pub channels_6g: Vec<u16>,
}

#[derive(Debug, Clone, Deserialize)]
pub struct Performance {
    pub ethernet_brokers: i32,
    pub wifi_brokers: i32,
    pub wifi_broker_buffer_capacity: usize,
    pub ethernet_broker_buffer_capacity: usize
}

#[derive(Debug, Clone, Deserialize)]
pub struct Misc {
    pub training_period_minutes: i32
}

#[derive(Debug, Clone, Deserialize)]
pub struct Protocols {
    pub tcp: ProtocolsTcp
}

#[derive(Debug, Clone, Deserialize)]
pub struct ProtocolsTcp {
    pub pipeline_size: i32,
    pub reassembly_buffer_size: i32,
    pub session_timeout_seconds: i32
}

pub fn load(path: String) -> Result<Configuration, Error> {
    let config_str = match read_to_string(path) {
        Ok(c) => c,
        Err(e) => bail!("Could not read configuration file. {}", e),
    };

    let substituted_config_str = match substitute_environment_variables(&config_str) {
        Ok(scs) => scs,
        Err(e) => bail!("Could not substitute environment variables in configuration file: {}", e)
    };

    let doc: Configuration = match toml::from_str(&substituted_config_str) {
        Ok(d) => d,
        Err(e) => bail!("Could not parse configuration. {}", e),
    };

    if doc.general.leader_secret.len() < 64 {
        bail!("Configuration variable `leader_secret` must be at least 64 characters long.");
    }

    if doc.misc.training_period_minutes <= 0 {
        bail!("Configuration variable `training_period_minutes` must be set to a value greater than 0.");
    }
    if doc.performance.ethernet_brokers <= 0 {
        bail!("Configuration variable `ethernet_brokers` must be set to a value greater than 0.");
    }

    if doc.performance.wifi_brokers <= 0 {
        bail!("Configuration variable `wifi_brokers` must be set to a value greater than 0.");
    }

    if doc.performance.wifi_broker_buffer_capacity == 0 {
        bail!("Configuration variable `wifi_pkt_buffer_capacity` must be set to a value greater than 0.");
    }

    if doc.performance.ethernet_broker_buffer_capacity == 0 {
        bail!("Configuration variable `ethernet_pkt_buffer_capacity` must be set to a value greater than 0.");
    }

    if doc.protocols.tcp.pipeline_size <= 0 {
        bail!("Configuration variable `protocols.tcp.pipeline_size` must be set to a value greater than 0.");
    }

    if doc.protocols.tcp.session_timeout_seconds <= 0 {
        bail!("Configuration variable `protocols.tcp.session_timeout_seconds` must be set to a value greater than 0.");
    }

    if doc.protocols.tcp.reassembly_buffer_size <= 0 {
        bail!("Configuration variable `protocols.tcp.reassembly_buffer_size` must be set to a value greater than 0.");
    }

    // Validate WiFi interfaces configuration
    if let Some(wifi_interfaces) = doc.clone().wifi_interfaces {
        let mut assigned_channels_2g: Vec<u16> = Vec::new();
        let mut assigned_channels_5g: Vec<u16> = Vec::new();
        let mut assigned_channels_6g: Vec<u16> = Vec::new();
        for interface in wifi_interfaces.values() {
            // Make sure every channel is only assigned once.
            for channel in &*interface.channels_2g {
                if assigned_channels_2g.contains(channel) {
                    bail!("WiFi channel <{}> on 2G band already assigned to another interface. Channels can only \
                                be assigned once.", *channel);
                } else {
                    assigned_channels_2g.push(*channel);
                }
            }
            for channel in &*interface.channels_5g {
                if assigned_channels_5g.contains(channel) {
                    bail!("WiFi channel <{}> on 5G band already assigned to another interface. Channels can only \
                                be assigned once.", *channel);
                } else {
                    assigned_channels_5g.push(*channel);
                }
            }
            for channel in &*interface.channels_6g {
                if assigned_channels_6g.contains(channel) {
                    bail!("WiFi channel <{}> on 6G band already assigned to another interface. Channels can only \
                                be assigned once.", *channel);
                } else {
                    assigned_channels_6g.push(*channel);
                }
            }
        }
    }

    // Test if URL can be parsed.
    match Url::parse(&doc.general.leader_uri) {
        Ok(..) => {},
        Err(e) => {
            bail!("Could not parse configuration variable `leader_uri` into URL. {}", e);
        }
    };

    Ok(doc)
}

fn substitute_environment_variables(config_str: &String) -> Result<String, Error> {
    // Extract all environment variables to substitute.
    let regex = Regex::new(r"\$\{(.+)\}");
    let mut requested_env_names: Vec<String> = Vec::new();
    for (_, [id]) in regex.unwrap().captures_iter(config_str).map(|c| c. extract()) {
        requested_env_names.push(id.to_string());
    }

    let mut substituted_string: String = config_str.clone();
    for requested in requested_env_names {
        // Check if requested environment variable exists.
        let env_value = match env::var(requested.clone()) {
            Ok(v) => v,
            Err(e) => bail!("Cannot substitute environment variable [{}]: {}", requested, e)
        };

        // Avoid endless recursion if someone passes a ENV variable replacement string as replacement.
        if env_value.contains('$') {
            bail!("Invalid character [$] in value of environment variable [{}]",  requested);
        }

        let mut search_var = "${".to_string();
        search_var.push_str(&requested);
        search_var.push('}');

        substituted_string = substituted_string.replace(search_var.as_str(), &env_value);
    }

    Ok(substituted_string.clone())
}
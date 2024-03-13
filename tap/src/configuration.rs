use std::collections::HashMap;
use std::{fs};
use std::fs::{File, read_to_string};
use std::path::{Path, PathBuf};

use anyhow::{Result, bail};
use reqwest::Url;
use serde::{Deserialize, Deserializer};
use serde::de::Error;

#[derive(Debug, Clone, Deserialize)]
pub struct Configuration {
    pub general: General,
    pub wifi_interfaces: Option<HashMap<String, WifiInterface>>,
    pub ethernet_interfaces: Option<HashMap<String, EthernetInterface>>,
    pub performance: Performance,
    pub misc: Misc
}

// Custom type so we can have deserializer with custom error message.
#[derive(Debug, Clone)]
pub struct DataDirectory {
    pub path: PathBuf
}

#[derive(Debug, Clone, Deserialize)]
pub struct General {
    pub leader_secret: String,
    pub leader_uri: String,
    pub accept_insecure_certs: bool,
    #[serde(deserialize_with = "deserialize_data_directory")]
    pub data_directory: DataDirectory
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

pub fn load(path: String) -> Result<Configuration, anyhow::Error> {
    let config_str = match read_to_string(path) {
        Ok(c) => c,
        Err(e) => bail!("Could not read configuration file. {}", e),
    };

    let doc: Configuration = match toml::from_str(&config_str) {
        Ok(d) => d,
        Err(e) => bail!("Could not parse configuration. {}", e),
    };

    if doc.general.leader_secret.len() < 64 {
        bail!("Configuration variable `leader_secret` must be at least 64 characters long.");
    }

    if !directory_is_readable(&doc.general.data_directory.path) {
        bail!("Configuration variable `data_directory` must be a path to a readable and \
                writable directory, but it is not readable. Resolved path was: {}", doc.general.data_directory.path.display());
    }

    if !directory_is_writable(&doc.general.data_directory.path) {
        bail!("Configuration variable `data_directory` must be a path to a readable and \
                writable directory but it is not writable. Resolved path was: {}", doc.general.data_directory.path.display());
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

fn deserialize_data_directory<'de, D>(deserializer: D) -> Result<DataDirectory, D::Error>
    where
        D: Deserializer<'de>,
{
    let s = String::deserialize(deserializer)?;

    match PathBuf::from(s).canonicalize() {
        Ok(path) => Ok(DataDirectory { path }),
        Err(e) => Err(D::Error::custom(format!("Could not deserialize `data_directory` \
                        configuration. Error was: {}", e)))
    }
}

fn directory_is_writable<P: AsRef<Path>>(path: P) -> bool {
    let path = path.as_ref().join(".temp_file_for_permission_check");

    File::create(&path).map(|_| {
        fs::remove_file(&path).unwrap(); // Unlikely that file removal would fail here.
        true
    }).unwrap_or(false)
}

fn directory_is_readable<P: AsRef<Path>>(path: P) -> bool {
    fs::read_dir(path).is_ok()
}

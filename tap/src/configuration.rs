use std::fs::read_to_string;

use anyhow::{Result, bail};
use reqwest::Url;
use serde::Deserialize;

#[derive(Debug, Clone, Deserialize)]
pub struct Configuration {
    pub general: General,
    pub ethernet: Ethernet,
    pub performance: Performance,
    pub misc: Misc
}

#[derive(Debug, Clone, Deserialize)]
pub struct General {
    pub leader_secret: String,
    pub leader_uri: String,
    pub accept_insecure_certs: bool
}

#[derive(Debug, Clone, Deserialize)]
pub struct Ethernet {
    pub ethernet_listen_interfaces: Vec<String>
}

#[derive(Debug, Clone, Deserialize)]
pub struct Performance {
    pub ethernet_brokers: i32
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

    if doc.misc.training_period_minutes <= 0 {
        bail!("Configuration variable `training_period_minutes` must be set to a value greater than 0.");
    }
    if doc.performance.ethernet_brokers <= 0 {
        bail!("Configuration variable `ethernet_brokers` must be set to a value greater than 0.");
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

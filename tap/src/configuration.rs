use anyhow::{Result, bail};
use hocon::HoconLoader;
use reqwest::Url;
use serde::Deserialize;

#[derive(Debug, Clone, Deserialize)]
pub struct Configuration {
    pub tap_name: String,
    pub leader_secret: String,
    pub leader_uri: String,
    pub accept_insecure_certs: bool,
    pub ethernet_listen_interfaces: Vec<String>,
    pub training_period_minutes: i32,
    pub ethernet_brokers: i32
}

pub fn load(path: String) -> Result<Configuration, anyhow::Error> {
    let doc: Configuration = HoconLoader::new()
        .no_url_include()
        .strict()
        .load_file(path)?
        .resolve()?;

    if doc.tap_name.trim().is_empty() {
        bail!("Configuration variable `tap_name` is empty");
    }

    if doc.leader_secret.len() < 64 {
        bail!("Configuration variable `leader_secret` must be at least 64 characters long.");
    }

    if doc.tap_name.len() > 50 {
        bail!("Configuration variable `tap_name` is longer than the allowed maximum 50 characters.");
    }

    if doc.training_period_minutes <= 0 {
        bail!("Configuration variable `training_period_minutes` must be set to a value greater than 0.");
    }
    if doc.ethernet_brokers <= 0 {
        bail!("Configuration variable `ethernet_brokers` must be set to a value greater than 0.");
    }

    // Test if URL can be parsed.
    match Url::parse(&doc.leader_uri) {
        Ok(..) => {},
        Err(e) => {
            bail!("Could not parse configuration variable `leader_uri` into URL. {}", e);
        }
    };

    Ok(doc)
}

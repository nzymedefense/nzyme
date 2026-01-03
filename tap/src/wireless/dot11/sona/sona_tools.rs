use anyhow::{bail, Error};

pub fn extract_serial_from_interface_name(prefix: &str, interface_name: &str) -> Result<String, Error> {
    if let Some(name) = interface_name.strip_prefix(&format!("{}-", prefix)).map(String::from) {
        if name.is_empty() {
            bail!("Invalid interface name: [{}]", interface_name);
        } else {
            Ok(name)
        }
    } else {
        bail!("Invalid interface name: [{}]", interface_name);
    }
}
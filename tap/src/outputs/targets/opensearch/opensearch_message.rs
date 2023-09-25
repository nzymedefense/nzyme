use std::collections::HashMap;
use std::sync::Arc;
use serde::Serialize;
use crate::dot11::frames::Dot11BeaconFrame;
use crate::outputs::output_data::OutputData;

#[derive(Serialize)]
#[serde(untagged)]
pub enum FieldValue {
    String(String),
    Long(i128),
    Boolean(bool),
    ArrayLong(Vec<u128>)
}

#[derive(Serialize)]
pub struct OpenSearchMessage {
    pub fields: HashMap<&'static str, FieldValue>
}

impl From<&Arc<Dot11BeaconFrame>> for OpenSearchMessage {
    fn from(f: &Arc<Dot11BeaconFrame>) -> Self {
        let mut fields = HashMap::from([
            ("@timestamp", FieldValue::String(f.receive_time.to_rfc3339())),
            ("message", FieldValue::String(f.get_message_summary())),
            ("service.name", FieldValue::String("nzyme".to_string())),
            ("nzyme.subsystem", FieldValue::String("wifi".to_string())),
            ("wifi.fingerprint", FieldValue::String(f.fingerprint.clone())),
            ("wifi.frame.type", FieldValue::String("management".to_string())),
            ("wifi.frame.subtype", FieldValue::String("beacon".to_string())),
            ("wifi.frame.length", FieldValue::Long(f.length as i128)),
            ("wifi.destination", FieldValue::String(f.destination.clone())),
            ("wifi.transmitter", FieldValue::String(f.transmitter.clone())),
            ("wifi.beacon.has_wps", FieldValue::Boolean(f.has_wps)),
            ("wifi.beacon.timestamp", FieldValue::Long(f.timestamp as i128)),
            ("wifi.beacon.interval", FieldValue::Long(f.interval as i128)),
            ("wifi.beacon.caps.infra_type", FieldValue::String(f.capabilities.infrastructure_type.to_string())),
            ("wifi.beacon.caps.privacy", FieldValue::Boolean(f.capabilities.privacy)),
            ("wifi.beacon.caps.short_preamble", FieldValue::Boolean(f.capabilities.short_preamble)),
            ("wifi.beacon.caps.pbcc", FieldValue::Boolean(f.capabilities.pbcc)),
            ("wifi.beacon.caps.channel_agility", FieldValue::Boolean(f.capabilities.channel_agility)),
            ("wifi.beacon.caps.short_slot_time", FieldValue::Boolean(f.capabilities.short_slot_time)),
            ("wifi.beacon.caps.dsss_ofdm", FieldValue::Boolean(f.capabilities.dsss_ofdm))
        ]);

        if let Some(is_wep) = f.header.is_wep {
            fields.insert("wifi.radiotap.is_wep", FieldValue::Boolean(is_wep));
        }

        if let Some(data_rate) = f.header.data_rate {
            fields.insert("wifi.radiotap.data_rate", FieldValue::Long(data_rate as i128));
        }

        if let Some(frequency) = f.header.frequency {
            fields.insert("wifi.radiotap.frequency", FieldValue::Long(frequency as i128));
        }

        if let Some(channel) = f.header.channel {
            fields.insert("wifi.radiotap.channel", FieldValue::Long(channel as i128));
        }

        if let Some(antenna_signal) = f.header.antenna_signal {
            fields.insert("wifi.radiotap.antenna_signal", FieldValue::Long(antenna_signal as i128));
        }

        if let Some(antenna) = f.header.antenna {
            fields.insert("wifi.radiotap.antenna", FieldValue::Long(antenna as i128));
        }

        if let Some(ssid) = f.tagged_parameters.ssid.clone() {
            fields.insert("wifi.beacon.tagged.ssid", FieldValue::String(ssid));
        }

        if f.tagged_parameters.supported_rates.is_some() || f.tagged_parameters.extended_supported_rates.is_some() {
            let mut all_rates = vec![];

            if let Some(rates) = f.tagged_parameters.supported_rates.clone() {
                rates.into_iter().for_each(|rate| all_rates.push(rate as u128));
            }

            if let Some(extended_rates) = f.tagged_parameters.extended_supported_rates.clone() {
                extended_rates.into_iter().for_each(|rate| all_rates.push(rate as u128));
            }

            fields.insert("wifi.beacon.tagged.all_supported_rates", FieldValue::ArrayLong(all_rates));
        }

        if let Some(country) = f.tagged_parameters.country_information.clone() {
            fields.insert("wifi.beacon.tagged.country.code", FieldValue::String(country.country_code));
            fields.insert("wifi.beacon.tagged.country.environment", FieldValue::String(country.environment.to_string()));
            fields.insert("wifi.beacon.tagged.country.first_channel", FieldValue::Long(country.first_channel as i128));
            fields.insert("wifi.beacon.tagged.country.channel_count", FieldValue::Long(country.channel_count as i128));
            fields.insert("wifi.beacon.tagged.country.max_transmit_power", FieldValue::Long(country.max_transmit_power as i128));
        }

        if let Some(pwnagotchi) = f.tagged_parameters.pwnagotchi_data.clone() {
            fields.insert("wifi.beacon.tagged.pwnagotchi.identity", FieldValue::String(pwnagotchi.identity));
            fields.insert("wifi.beacon.tagged.pwnagotchi.name", FieldValue::String(pwnagotchi.name));
            fields.insert("wifi.beacon.tagged.pwnagotchi.version", FieldValue::String(pwnagotchi.version));
            fields.insert("wifi.beacon.tagged.pwnagotchi.uptime", FieldValue::Long(pwnagotchi.uptime as i128));
            fields.insert("wifi.beacon.tagged.pwnagotchi.pwnd_run", FieldValue::Long(pwnagotchi.pwnd_run as i128));
            fields.insert("wifi.beacon.tagged.pwnagotchi.pwnd_tot", FieldValue::Long(pwnagotchi.pwnd_tot as i128));
        }

        OpenSearchMessage {
            fields
        }
    }
}
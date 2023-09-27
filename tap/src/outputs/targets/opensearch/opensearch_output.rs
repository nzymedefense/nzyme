use std::sync::Arc;
use std::time::Duration;
use log::{error, info};
use reqwest::blocking::{Body, Client};
use reqwest::Url;
use crate::dot11::frames::Dot11BeaconFrame;
use crate::ethernet::packets::DNSPacket;
use crate::outputs::targets::opensearch::opensearch_message::OpenSearchMessage;
use crate::outputs::targets::output_message_receiver::OutputMessageReceiver;

pub struct OpenSearchOutput {
    http_client: Client
}

impl OpenSearchOutput {

    pub fn new() -> Self {
        let http_client = reqwest::blocking::Client::builder()
            .timeout(Duration::from_secs(10))
            .user_agent("nzyme")
            .gzip(true)
            .build()
            .unwrap();

        OpenSearchOutput {
            http_client
        }
    }

    fn send(&self, json: String) {
        let result = self.http_client
            .post(Url::parse("http://100.81.142.139:2021/log/ingest").unwrap())
            .body(Body::from(json))
            .send();

        if let Err(e) = result {
            error!("Could not send OpenSearch output message: {}", e);
        }
    }
}

// MAKE BUFFER GLOBAL. WRITE STRING TO VEC

impl OutputMessageReceiver for OpenSearchOutput {
    fn write_dns_packets(&self, packets: &Vec<Arc<DNSPacket>>) {
        for slice in packets.chunks(50) {
            let mut payload = vec![];
            for message in slice {
                let x: OpenSearchMessage = message.into();
                payload.push(x.fields);
            }

            let json: String = serde_json::to_string(&payload)
                .unwrap();

            self.send(json);
        }
    }

    fn write_dot11_beacon_frames(&self, frames: &Vec<Arc<Dot11BeaconFrame>>) {
        for slice in frames.chunks(50) {
            let mut payload = vec![];
            for message in slice {
                let x: OpenSearchMessage = message.into();
                payload.push(x.fields);
            }

            let json: String = serde_json::to_string(&payload)
                .unwrap();

            self.send(json);
        }
    }
}
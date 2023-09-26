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

}

impl OutputMessageReceiver for OpenSearchOutput {
    fn write_dns_packet(&self, dns: &Arc<DNSPacket>) {
        info!("DNS: {:?}", dns);
    }

    fn write_dot11_beacon_frame(&self, frame: &Arc<Dot11BeaconFrame>) {
        let json: String = serde_json::to_string(&vec![OpenSearchMessage::from(frame).fields])
            .unwrap();

        // TODO send to a self.send(json: String) thing
        let result = self.http_client
            .post(Url::parse("http://100.81.142.139:2021/log/ingest").unwrap())
            .body(Body::from(json))
            .send();

        if let Err(e) = result {
            error!("Could not send OpenSearch output message: {}", e);
        }
    }
}
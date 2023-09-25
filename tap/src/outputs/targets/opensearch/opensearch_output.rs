use std::sync::Arc;
use std::time::Duration;
use log::info;
use reqwest::blocking::Body;
use reqwest::Url;
use crate::dot11::frames::Dot11BeaconFrame;
use crate::outputs::targets::opensearch::opensearch_message::OpenSearchMessage;
use crate::outputs::targets::output_message_receiver::OutputMessageReceiver;

pub struct OpenSearchOutput {

}

impl OpenSearchOutput {

}

impl OutputMessageReceiver for OpenSearchOutput {
    fn write_dot11_beacon_frame(&self, frame: &Arc<Dot11BeaconFrame>) {
        let json: String = serde_json::to_string(&vec![OpenSearchMessage::from(frame).fields])
            .unwrap();

        info!("BEACON IN OPENSEARCH OUTPUT: {:?}", json);

        let http_client = reqwest::blocking::Client::builder()
            .timeout(Duration::from_secs(10))
            .user_agent("nzyme")
            .gzip(true)
            .build()
            .unwrap();

        http_client
            .post(Url::parse("http://4.151.135.9:2021/log/ingest").unwrap())
            .body(Body::from(json))
            .send()
            .unwrap();
    }
}
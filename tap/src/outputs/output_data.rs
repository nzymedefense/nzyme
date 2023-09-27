/*
 * A specific output data type definition allows us to clearly define what type of data is
 * supported for forwarding and not having to assume that all data can be forwarded.
 */
use crate::outputs::output_configuration::OutputConfiguration;

#[derive(Debug)]
pub enum OutputDataType {
    Dot11Beacon
}

pub enum OutputFilterResult {
    Block, Pass
}

pub enum OutputTarget {
    OpenSearch,
    Splunk
}

pub trait OutputData {

    fn get_message_summary(&self) -> String;
    fn filter(&self, output_configuration: &OutputConfiguration) -> OutputFilterResult;

}
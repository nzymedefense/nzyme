use std::cmp::min;
use crate::protocols::detection::taggers::tagger_utils;

static METHODS: &[&str] = &["get", "post", "put", "delete", "head", "options", "patch"];
static VERSIONS: &[&str] = &["HTTP/1.0", "HTTP/1.1", "HTTP/2", "HTTP/3"];

pub fn tag(cts: &[u8], stc: &[u8]) -> Option<()> {
    // Convert up to the first 255 bytes to lowercase string.
    let client_to_server_string = String::from_utf8_lossy(&cts[..min(cts.len(), 255)]).to_string().to_lowercase();
    let server_to_client_string = String::from_utf8_lossy(&stc[..min(stc.len(), 255)]).to_string().to_lowercase();

    if tagger_utils::scan_body_substrings_or(&client_to_server_string, METHODS)
        && tagger_utils::scan_body_substrings_or(&client_to_server_string, VERSIONS)
        && tagger_utils::scan_body_substring(&client_to_server_string, "\r\n")
        && tagger_utils::scan_body_substrings_or(&server_to_client_string, VERSIONS)
        && tagger_utils::scan_body_substring(&server_to_client_string, "\r\n") {

        Some(())
    } else {
        None
    }
}
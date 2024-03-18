use crate::ethernet::detection::taggers::tagger_utils;

pub fn tag(cts: &String, stc: &String) -> bool {
    let methods = &vec!["get", "post", "put", "delete", "head", "options", "patch"];
    let versions = &vec!["HTTP/1.0", "HTTP/1.1", "HTTP/2", "HTTP/3"];

    let lowercase_cts = &cts.to_lowercase();
    let lowercase_stc = &stc.to_lowercase();

    tagger_utils::scan_body_substrings_or(lowercase_cts, methods)
        && tagger_utils::scan_body_substrings_or(lowercase_cts, versions)
        && tagger_utils::scan_body_substring(lowercase_cts, "\r\n")
        && tagger_utils::scan_body_substrings_or(lowercase_stc, versions)
        && tagger_utils::scan_body_substring(lowercase_stc, "\r\n")
}
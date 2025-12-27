pub fn extract_serial_from_interface_name(interface_name: &str) -> Option<String> {
    interface_name.strip_prefix("sona-").map(String::from)
}
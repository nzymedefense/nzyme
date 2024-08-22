pub fn mac_is_locally_administered_or_multicast(mac: &str) -> bool {
    // Extract the first byte (two characters) of the MAC address and convert to an integer
    if let Some(first_byte_str) = mac.split(':').next() {
        if let Ok(first_byte) = u8::from_str_radix(first_byte_str, 16) {
            // Check if the second least significant bit (locally administered) or the least significant bit (multicast) is set
            return (first_byte & 0b00000010 != 0) || (first_byte & 0b00000001 != 0);
        }
    }

    false
}
pub fn mac_is_locally_administered_and_multicast(mac: &str) -> bool {
    // Extract the first byte (two characters) of the MAC address and convert to an integer
    if let Some(first_byte_str) = mac.split(':').next() {
        if let Ok(first_byte) = u8::from_str_radix(first_byte_str, 16) {
            // Check if the first two bits are set (i.e., 0b11)
            return (first_byte & 0b00000010 != 0) && (first_byte & 0b00000001 != 0);
        }
    }

    false
}
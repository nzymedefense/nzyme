pub fn to_mac_address_string(bytes: &[u8]) -> String {
    assert!(bytes.len() == 6);

    format!("{:02X?}:{:02X?}:{:02X?}:{:02X?}:{:02X?}:{:02X?}",
        bytes[0], bytes[1], bytes[2], bytes[3], bytes[4], bytes[5])
}

pub fn to_ipv4_address_string(bytes: &[u8]) -> String {
    assert!(bytes.len() == 4);

    format!("{}.{}.{}.{}", bytes[0], bytes[1], bytes[2], bytes[3])
}

use log::error;

pub fn to_mac_address_string(bytes: &[u8]) -> String {
    assert!(bytes.len() == 6);

    format!("{:02X?}:{:02X?}:{:02X?}:{:02X?}:{:02X?}:{:02X?}",
        bytes[0], bytes[1], bytes[2], bytes[3], bytes[4], bytes[5])
}

pub fn to_ipv4_address_string(bytes: &[u8]) -> String {
    assert!(bytes.len() == 4);

    format!("{}.{}.{}.{}", bytes[0], bytes[1], bytes[2], bytes[3])
}


pub fn is_mac_address_multicast(mac: &String) -> bool {
    if mac.len() != 17 {
        error!("Invalid MAC address [{}] cannot determine if it is a multicast address or not.", mac);
        return false;
    }
    
    // Parse the first octet
    let first_octet = match u8::from_str_radix(&mac[0..2], 16) {
        Ok(fo) => fo,
        Err(_) => {
            error!("Invalid MAC address [{}] cannot determine if it is a multicast address or not.", mac);
            return false;
        }
    };
    
    first_octet & 0x01 == 1
}
use anyhow::{bail, Error};
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

pub fn dot11_frequency_to_channel(f: u16) -> Result<u16, Error> {
    match f {
        2412 => Ok(1),
        2417 => Ok(2),
        2422 => Ok(3),
        2427 => Ok(4),
        2432 => Ok(5),
        2437 => Ok(6),
        2442 => Ok(7),
        2447 => Ok(8),
        2452 => Ok(9),
        2457 => Ok(10),
        2462 => Ok(11),
        2467 => Ok(12),
        2472 => Ok(13),
        2484 => Ok(14),

        5160 => Ok(32),
        5180 => Ok(36),
        5190 => Ok(38),
        5200 => Ok(40),
        5210 => Ok(42),
        5220 => Ok(44),
        5230 => Ok(46),
        5240 => Ok(48),
        5250 => Ok(50),
        5260 => Ok(52),
        5270 => Ok(54),
        5280 => Ok(56),
        5290 => Ok(58),
        5300 => Ok(60),
        5310 => Ok(62),
        5320 => Ok(64),
        5340 => Ok(68),
        5480 => Ok(96),
        5500 => Ok(100),
        5510 => Ok(102),
        5520 => Ok(104),
        5530 => Ok(106),
        5540 => Ok(108),
        5550 => Ok(110),
        5560 => Ok(112),
        5570 => Ok(114),
        5580 => Ok(116),
        5590 => Ok(118),
        5600 => Ok(120),
        5610 => Ok(122),
        5620 => Ok(124),
        5630 => Ok(126),
        5640 => Ok(128),
        5650 => Ok(130),
        5660 => Ok(132),
        5670 => Ok(134),
        5680 => Ok(136),
        5690 => Ok(138),
        5700 => Ok(140),
        5710 => Ok(142),
        5720 => Ok(144),
        5745 => Ok(149),
        5755 => Ok(151),
        5765 => Ok(153),
        5775 => Ok(155),
        5785 => Ok(157),
        5795 => Ok(159),
        5805 => Ok(161),
        5815 => Ok(163),
        5825 => Ok(165),
        5835 => Ok(167),
        5845 => Ok(169),
        5855 => Ok(171),
        5865 => Ok(173),
        5875 => Ok(175),
        5885 => Ok(177),
        4920 => Ok(184),
        4940 => Ok(188),
        4960 => Ok(192),
        4980 => Ok(196),
        _ => bail!("Unknown channel for frequency <{}>", f)
    }
}

pub fn dot11_channel_to_frequency(f: u16) -> Result<u16, Error> {
    match f {
        1 => Ok(2412),
        2 => Ok(2417),
        3 => Ok(2422),
        4 => Ok(2427),
        5 => Ok(2432),
        6 => Ok(2437),
        7 => Ok(2442),
        8 => Ok(2447),
        9 => Ok(2452),
        10 => Ok(2457),
        11 => Ok(2462),
        12 => Ok(2467),
        13 => Ok(2472),
        14 => Ok(2484),
        32 => Ok(5160),
        36 => Ok(5180),
        38 => Ok(5190),
        40 => Ok(5200),
        42 => Ok(5210),
        44 => Ok(5220),
        46 => Ok(5230),
        48 => Ok(5240),
        50 => Ok(5250),
        52 => Ok(5260),
        54 => Ok(5270),
        56 => Ok(5280),
        58 => Ok(5290),
        60 => Ok(5300),
        62 => Ok(5310),
        64 => Ok(5320),
        68 => Ok(5340),
        96 => Ok(5480),
        100 => Ok(5500),
        102 => Ok(5510),
        104 => Ok(5520),
        106 => Ok(5530),
        108 => Ok(5540),
        110 => Ok(5550),
        112 => Ok(5560),
        114 => Ok(5570),
        116 => Ok(5580),
        118 => Ok(5590),
        120 => Ok(5600),
        122 => Ok(5610),
        124 => Ok(5620),
        126 => Ok(5630),
        128 => Ok(5640),
        130 => Ok(5650),
        132 => Ok(5660),
        134 => Ok(5670),
        136 => Ok(5680),
        138 => Ok(5690),
        140 => Ok(5700),
        142 => Ok(5710),
        144 => Ok(5720),
        149 => Ok(5745),
        151 => Ok(5755),
        153 => Ok(5765),
        155 => Ok(5775),
        157 => Ok(5785),
        159 => Ok(5795),
        161 => Ok(5805),
        163 => Ok(5815),
        165 => Ok(5825),
        167 => Ok(5835),
        169 => Ok(5845),
        171 => Ok(5855),
        173 => Ok(5865),
        175 => Ok(5875),
        177 => Ok(5885),
        184 => Ok(4920),
        188 => Ok(4940),
        192 => Ok(4960),
        196 => Ok(4980),
        _ => bail!("Unknown frequency for channel <{}>", f)
    }
}
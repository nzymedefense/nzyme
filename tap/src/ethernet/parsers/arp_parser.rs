use std::sync::Arc;

use byteorder::{BigEndian, ByteOrder};
use log::trace;
use anyhow::{Result, anyhow};

use crate::ethernet::packets::{EthernetPacket, ARPPacket};
use crate::ethernet::types::{find_hardwaretype, find_ethertype, find_arp_opcode};
use crate::helpers::network::{to_mac_address_string, to_ipv4_address_string};

pub fn parse(packet: &Arc<EthernetPacket>) -> Result<ARPPacket> {
    trace!("Received ARP packet: {:?}", &packet.data);

    if packet.data.len() < 8 {
        return Err(anyhow!("ARP packet is too small. Wouldn't even fit known-length fields. \
                            Skipping. {:?}", &packet.data));
    }

    let hardware_type = find_hardwaretype(BigEndian::read_u16(&packet.data[0..2]));
    let protocol_type = find_ethertype(BigEndian::read_u16(&packet.data[2..4]));
    let hardware_length = packet.data[4];
    let protocol_length = packet.data[5];
    let operation = find_arp_opcode(BigEndian::read_u16(&packet.data[6..8]));

    if hardware_length != 6 || protocol_length != 4 {
        return Err(anyhow!("ARP packet is not using ethernet/ipv4 combination. Not supported. \
                            Skipping. {:?}", &packet.data));
    }

    let mut start = 8;
    let mut end = start+hardware_length;
    let sender_hardware_address = to_mac_address_string(&packet.data[start as usize..end as usize]);
   
    start = end;
    end += protocol_length;
    let sender_protocol_address = to_ipv4_address_string(&packet.data[start as usize..end as usize]);

    start = end;
    end += hardware_length;
    let target_hardware_address = to_mac_address_string(&packet.data[start as usize..end as usize]);
    
    start = end;
    end += protocol_length;
    let target_protocol_address = to_ipv4_address_string(&packet.data[start as usize..end as usize]);

    if packet.data.len() < (8+(hardware_length*2)+(protocol_length*2)).into() {
        return Err(anyhow!("ARP packet is too small. Wouldn't fit addresses. \
                            Skipping. {:?}", &packet.data));
    }

    let p = ARPPacket {
        source_mac: packet.source_mac.clone(),
        destination_mac: packet.destination_mac.clone(),
        hardware_type,
        protocol_type,
        hardware_length,
        protocol_length,
        operation,
        sender_hardware_address,
        sender_protocol_address,
        target_hardware_address,
        target_protocol_address,
        size: 40,
        timestamp: packet.timestamp
    };

    Ok(p)
}

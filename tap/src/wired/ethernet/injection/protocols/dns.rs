use std::net::Ipv4Addr;

pub fn ptr_query(ip: Ipv4Addr, transaction_id: u16) -> Vec<u8> {
    let mut query: Vec<u8> = Vec::new();

    // DNS Header.
    query.extend_from_slice(&transaction_id.to_be_bytes()); // Transaction ID.

    let flags: u16 = 0x0100; // Standard query.
    query.extend_from_slice(&flags.to_be_bytes()); // Flags

    let qdcount: u16 = 1; // One question
    query.extend_from_slice(&qdcount.to_be_bytes()); // Number of questions.

    let ancount: u16 = 0;
    query.extend_from_slice(&ancount.to_be_bytes()); // Number of answers.

    let nscount: u16 = 0;
    query.extend_from_slice(&nscount.to_be_bytes()); // Number of authority records.

    let arcount: u16 = 0;
    query.extend_from_slice(&arcount.to_be_bytes()); // Number of additional records.

    // Question Section.
    let octets = ip.octets();
    let ptr_name = format!("{}.{}.{}.{}.in-addr.arpa", octets[3], octets[2], octets[1], octets[0]);

    for label in ptr_name.split('.') {
        query.push(label.len() as u8); // Length of the label.
        query.extend_from_slice(label.as_bytes()); // Label.
    }

    query.push(0); // End of the QNAME.

    let qtype: u16 = 12; // PTR query
    query.extend_from_slice(&qtype.to_be_bytes()); // QTYPE.

    let qclass: u16 = 1; // IN class
    query.extend_from_slice(&qclass.to_be_bytes()); // QCLASS.

    query
}

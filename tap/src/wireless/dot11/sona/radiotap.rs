pub fn create_radiotap_header(flags: u8, rate: u8, frequency: u16, rssi: f32, tsft_us: u64, ) -> Vec<u8> {
    let mut radiotap = Vec::new();

    radiotap.push(0); // Version.
    radiotap.push(0); // Pad.
    radiotap.extend_from_slice(&0u16.to_le_bytes()); // Length placeholder.

    let present_word0: u32 =
        (1u32 << 0)  | // TSFT
            (1u32 << 1)  | // Flags
            (1u32 << 2)  | // Rate
            (1u32 << 3)  | // Channel
            (1u32 << 5)  | // dBm Antenna Signal
            (1u32 << 11) | // Antenna
            (1u32 << 14);  // RX Flags
    radiotap.extend_from_slice(&present_word0.to_le_bytes());

    // TSFT
    radiotap.extend_from_slice(&tsft_us.to_le_bytes());

    // Flags.
    radiotap.push(flags);

    // Rate.
    radiotap.push(rate);

    // Channel frequency + channel flags.
    radiotap.extend_from_slice(&frequency.to_le_bytes());
    let channel_flags: u16 = 0;
    radiotap.extend_from_slice(&channel_flags.to_le_bytes());

    // dBm Antenna Signal.
    let sig: i8 = rssi.round().clamp(i8::MIN as f32, i8::MAX as f32) as i8;
    radiotap.push(sig as u8);

    // Antenna.
    radiotap.push(0);

    // RX Flags (2-byte aligned).
    let rx_flags: u16 = 0;
    radiotap.extend_from_slice(&rx_flags.to_le_bytes());

    // Set final length.
    let rt_len = radiotap.len() as u16;
    radiotap[2..4].copy_from_slice(&rt_len.to_le_bytes());

    radiotap
}
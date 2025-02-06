use log::info;

pub fn tag(data: &[u8]) -> Option<()> {
    if data.len() < 6 {
        return None
    }

    let frame_count = data[4];
    let message_type = (data[5] & 0xF0) >> 4;
    let protocol_version = data[5] & 0x0F;

    /*
     * We support both of the existing F3411 versions. The original version "19" has not been
     * significantly changed on the protocol level and is backwards-compatible. We do not need
     * to parse it differently.
     */
    if protocol_version != 1 && protocol_version != 2 {
        // Not a supported version or not a Remote ID message.
        return None
    }
    
    
    
    if message_type == 15 {
        info!("Drone. Frame {}: Type {} (MESSAGE_PACK) Version {} (F3411-22)", frame_count, message_type, protocol_version);
    }

    None
}
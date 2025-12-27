#[derive(Debug)]
pub enum SonaCommand {
    SetFrequency(u32)
}

#[derive(Debug)]
pub struct AddressedSonaCommand {
    pub sona_device_serial: String,
    pub cmd: SonaCommand,
}
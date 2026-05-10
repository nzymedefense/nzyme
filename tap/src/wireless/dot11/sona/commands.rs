use crate::wireless::dot11::sona::protocol::SonaFilter;

#[derive(Debug)]
pub enum SonaCommand {
    SetFrequency(u16),
    SetFilter(SonaFilter),
}

#[derive(Debug)]
pub struct AddressedSonaCommand {
    pub sona_device_serial: String,
    pub cmd: SonaCommand,
}
pub const NZ_MAGIC: [u8; 2] = *b"NZ";
pub const V1_HEADER_LEN: u16 = 84;

// Static for now, but could be dynamic or change in the future.
pub const HEADER_VERSION: u8 = 1;
pub const FILE_TYPE_FIRMWARE_BINARY: u8 = 0;
pub const NZYME_USB_VENDOR_ID: u16 = 0x390C;
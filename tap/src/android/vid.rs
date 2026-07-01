/*
 * Known USB vendor IDs for Android device manufacturers.
 * Sourced from Google vendor list and android-udev.
 *
 * https://developer.android.com/studio/run/device#VendorIds
 */
const ANDROID_VIDS: &[u16] = &[
    0x0502, // Acer
    0x0b05, // Asus
    0x0489, // Foxconn (some Pixel/Google variants)
    0x091e, // Garmin-Asus
    0x18d1, // Google / Nexus / Pixel
    0x109b, // Hisense
    0x0bb4, // HTC / OnePlus (older)
    0x12d1, // Huawei
    0x8087, // Intel
    0x24e3, // K-Touch
    0x2116, // KT Tech
    0x0482, // Kyocera
    0x17ef, // Lenovo
    0x1004, // LG
    0x0e8d, // MediaTek
    0x22b8, // Motorola
    0x0409, // NEC
    0x2080, // Nook
    0x0955, // Nvidia
    0x2a70, // OnePlus (newer)
    0x2257, // OTGV
    0x10a9, // Pantech
    0x1d4d, // Pegatron
    0x0471, // Philips
    0x05c6, // Qualcomm
    0x04e8, // Samsung
    0x04dd, // Sharp
    0x1f53, // SK Telesys
    0x054c, // Sony
    0x0fce, // Sony Mobile
    0x2340, // Teleepoch
    0x0930, // Toshiba
    0x2717, // Xiaomi
    0x19d2, // ZTE
];

pub fn is_android_vid(vid: u16) -> bool {
    ANDROID_VIDS.contains(&vid)
}
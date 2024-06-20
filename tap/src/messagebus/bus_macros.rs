#[macro_export]
macro_rules! to_pipeline {
    ($name:expr, $sender:expr, $packet: expr, $len: expr) => {
        match $sender.lock() {
            Ok(mut sender) => sender.send_packet($packet, $len),
            Err(e) => error!("Could not acquire sender mutex of channel [{:?}]: {}", $name, e)
        }
    };
}
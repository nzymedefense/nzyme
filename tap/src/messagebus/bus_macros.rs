#[macro_export]
macro_rules! to_pipeline {
    ($name:expr_2021, $sender:expr_2021, $packet: expr_2021, $len: expr_2021) => {
        match $sender.lock() {
            Ok(mut sender) => sender.send_packet($packet, $len),
            Err(e) => error!("Could not acquire sender mutex of channel [{:?}]: {}", $name, e)
        }
    };
}
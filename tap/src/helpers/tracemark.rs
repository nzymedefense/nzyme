#[macro_export]
macro_rules! tracemark {
    ($msg:expr_2021) => {
        log::trace!("TRACEMARK {}:{}: {}", file!(), line!(), $msg);
    };
    ($fmt:expr_2021, $($arg:tt)*) => {
        log::trace!(concat!("TRACEMARK {}:{} -> ", $fmt), file!(), line!(), $($arg)*);
    };
}
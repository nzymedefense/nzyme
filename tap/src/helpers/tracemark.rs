#[macro_export]
macro_rules! tracemark {
    ($msg:expr) => {
        log::trace!("TRACEMARK {}:{}: {}", file!(), line!(), $msg);
    };
    ($fmt:expr, $($arg:tt)*) => {
        log::trace!(concat!("TRACEMARK {}:{} -> ", $fmt), file!(), line!(), $($arg)*);
    };
}
use std::sync::Mutex;
use anyhow::{bail, Error};
use log::Level;

#[derive(Default)]
pub struct LogMonitor {
    counts: Mutex<LogCounts>
}

#[derive(Clone, Default, Debug)]
pub struct LogCounts {
    pub error: u128,
    pub warn: u128,
    pub info: u128,
    pub debug: u128,
    pub trace: u128
}

impl LogMonitor {

    pub fn mark(&self, level: &Level) {
        // DO NOT LOG HERE. This will run the entire logic recursively and blow up the stack.
        if let Ok(mut counts) = self.counts.lock() {
            match level {
                Level::Error => counts.error += 1,
                Level::Warn => counts.warn += 1,
                Level::Info => counts.info += 1,
                Level::Debug => counts.debug += 1,
                Level::Trace => counts.trace += 1,
            }
        }
    }

    pub fn get_counts(&self) -> Result<LogCounts, Error> {
        match self.counts.lock() {
            Ok(counts) => Ok(counts.clone()),
            Err(e) => bail!("Could not acquire log counts mutex: {}", e)
        }
    }

    pub fn reset_counts(&self) -> Result<(), Error> {
        match self.counts.lock() {
            Ok(mut counts) => {
                counts.error = 0;
                counts.warn = 0;
                counts.info = 0;
                counts.debug = 0;
                counts.trace = 0;

                Ok(())
            },
            Err(e) => bail!("Could not acquire log counts mutex: {}", e)
        }
    }

}
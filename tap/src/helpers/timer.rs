use std::sync::Mutex;
use chrono::{DateTime, Utc};
use log::error;
use crate::metrics::Metrics;

pub struct Timer {
    start_time: DateTime<Utc>,
    end_time: Option<DateTime<Utc>>
}

impl Timer {

    pub fn new() -> Self {
        Self {
            start_time: Utc::now(),
            end_time: None
        }
    }

    pub fn stop(&mut self) {
        self.end_time = Some(Utc::now());
    }

    pub fn elapsed_microseconds(&self) -> Option<i64> {
        match self.end_time {
            Some(end_time) => Some((end_time - self.start_time).num_microseconds().unwrap()),
            None => None
        }
    }

}

pub fn record_timer(elapsed_microseconds: Option<i64>, timer_name: &str, metrics: &Mutex<Metrics>) {
    if elapsed_microseconds.is_none(){
        error!("Passed empty duration for timer {}.", timer_name);
        return
    }

    match metrics.lock() {
        Ok(mut metrics) => {
            metrics.record_timer(timer_name, elapsed_microseconds.unwrap());
        },
        Err(e) => {
            error!("Could not acquire metrics lock to write timer {}: {}", timer_name, e);
        }
    }
}
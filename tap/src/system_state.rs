use std::{thread, time, sync::{atomic::{AtomicBool, Ordering}, Arc}};

use log::info;

pub struct SystemState {
    in_training: Arc<AtomicBool>,
    pub training_period_minutes: usize
}

impl SystemState {

    pub fn new(training_period_minutes: usize) -> Self {
        SystemState {
            in_training: Arc::new(AtomicBool::new(true)),
            training_period_minutes
        }
    }

    pub fn initialize(self) -> Self {
        let in_training = self.in_training.clone();
        let training_period_minutes = self.training_period_minutes;

        thread::spawn(move || {
            thread::sleep(time::Duration::from_secs((training_period_minutes*60) as u64));

            in_training.store(false, Ordering::Relaxed);
            info!("Training period ended after <{}> minutes.", training_period_minutes);
        });

        self
    }

    pub fn is_in_training(&self) -> bool {
        self.in_training.load(Ordering::Relaxed)
    }

}
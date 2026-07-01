use std::collections::VecDeque;
use std::sync::Mutex;
use chrono::{DateTime, Local};
use log::Level;

const MAX_LINES: usize = 100;

#[derive(Clone,)]
pub struct LogBufferLine {
    pub timestamp: DateTime<Local>,
    pub level: Level,
    pub message: String
}

pub struct LogBuffer {
    lines: Mutex<VecDeque<LogBufferLine>>,
}

impl LogBuffer {
    pub fn new() -> Self {
        Self {
            lines: Mutex::new(VecDeque::with_capacity(MAX_LINES)),
        }
    }

    pub fn push(&self, line: LogBufferLine) {
        if let Ok(mut buf) = self.lines.lock() {
            if buf.len() == MAX_LINES {
                buf.pop_front();
            }
            buf.push_back(line);
        }
    }

    pub fn snapshot(&self) -> Vec<LogBufferLine> {
        match self.lines.lock() {
            Ok(buf) => buf.iter().cloned().collect(),
            Err(_) => Vec::new(),
        }
    }
}
use std::collections::HashMap;
use std::sync::Mutex;
use log::error;

pub fn clear_mutex_hashmap<X, T>(table: &Mutex<HashMap<X, T>>) {
    match table.lock() {
        Ok(mut table) => table.clear(),
        Err(e) =>  error!("Could not acquire table mutex to clear HashMap: {}", e)
    };
}

pub fn clear_mutex_vector<T>(vec: &Mutex<Vec<T>>) {
    match vec.lock() {
        Ok(mut vec) => vec.clear(),
        Err(e) =>  error!("Could not acquire table mutex to clear Vector: {}", e)
    };
}
use std::collections::HashMap;
use std::sync::Mutex;
use log::error;

pub fn clear_mutex_hashmap<X, T>(table: &Mutex<HashMap<X, T>>) {
    match table.lock() {
        Ok(mut table) => table.clear(),
        Err(e) =>  error!("Could not acquire mutex to clear HashMap: {}", e)
    };
}

pub fn clear_mutex_vector<T>(vec: &Mutex<Vec<T>>) {
    match vec.lock() {
        Ok(mut vec) => vec.clear(),
        Err(e) =>  error!("Could not acquire mutex to clear Vector: {}", e)
    };
}

pub fn get_mutex_hashmap_size<X, T>(m: &Mutex<HashMap<X, T>>) -> i128 {
    match m.lock() {
        Ok(map) => map.len() as i128,
        Err(e) => {
            error!("Could not acquire mutex to determine size of HashMap: {}", e);
            -1
        }
    }
}

pub fn get_mutex_vector_size<T>(m: &Mutex<Vec<T>>) -> i128 {
    match m.lock() {
        Ok(vec) => vec.len() as i128,
        Err(e) => {
            error!("Could not acquire mutex to determine size of Vector: {}", e);
            -1
        }
    }
}
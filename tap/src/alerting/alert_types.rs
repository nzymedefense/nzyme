use std::collections::HashMap;
use serde::Serialize;

#[derive(Serialize, Clone, Debug)]
pub enum Dot11AlertType {
    PwnagotchiDetected
}

#[derive(Serialize, Clone, Debug)]
pub enum Dot11AlertAttribute {
    Number(u128),
    String(String)
}

#[derive(Debug)]
pub struct Dot11Alert {
    pub alert_type: Dot11AlertType,
    pub attributes: HashMap<String, Dot11AlertAttribute>
}
use std::fs;

const MODEL_PATH: &str = "/sys/firmware/devicetree/base/model";

pub fn detect_pi_model() -> Option<String> {
    if let Ok(model) = fs::read_to_string(MODEL_PATH) && model.starts_with("Raspberry Pi") {
         return Some(model.trim_matches(char::from(0)).to_string());
    }

    None
}
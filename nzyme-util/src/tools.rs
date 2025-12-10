use std::path::PathBuf;

pub fn as_path_buf(s: impl Into<String>) -> PathBuf {
    PathBuf::from(s.into())
}
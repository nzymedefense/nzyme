use regex::Regex;

pub fn scan_body_substring(body: &String, substring: &str) -> bool {
    body.contains(substring)
}

pub fn scan_body_substrings_or(body: &String, substrings: &[&str]) -> bool {
    for substring in substrings {
        if body.contains(&substring.to_lowercase()) {
            return true
        }
    }

    false
}

pub fn scan_body_substrings_and(body: &String, substrings: &[&str]) -> bool {
    for substring in substrings {
        if !body.contains(&substring.to_lowercase()) {
            return false
        }
    }

    true
}

pub fn scan_body_regex(body: &String, regex: &Regex) -> bool {
    regex.is_match(body)
}

fn clean_body(body: &String) -> String {
    return body.chars()
        .filter(|c| !c.is_control())
        .collect::<String>()
        .to_lowercase();
}
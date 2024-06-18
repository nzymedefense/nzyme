pub fn optional_size<T, F>(x: &Option<T>, func: F) -> u32
where
    F: FnOnce(&T) -> u32,
{
    x.as_ref().map_or(0, func)
}
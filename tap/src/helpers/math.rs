#[allow(clippy::cast_precision_loss)]
pub fn mean(data: &[f32]) -> Option<f32> {
    let sum = data.iter().sum::<f32>();
    let count = data.len();

    match count {
        positive if positive > 0 => Some(sum / count as f32),
        _ => None,
    }
}

#[allow(clippy::cast_precision_loss)]
pub fn std_deviation(data: &[f32]) -> Option<f32> {
    let len = data.len();
    let mean = mean(data);

    match (mean, len) {
        (Some(data_mean), count) if count > 0 => {
            let variance = data.iter().map(|value| {
                let diff = data_mean - *value;

                diff * diff
            }).sum::<f32>() / count as f32;

            Some(variance.sqrt())
        },
        _ => None
    }
}
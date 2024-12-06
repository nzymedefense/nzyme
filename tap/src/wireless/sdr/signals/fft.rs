use num_complex::{Complex, Complex32};
use rustfft::FftPlanner;

pub fn calculate_power_spectrum(samples: &[Complex32]) -> Vec<f64> {
    let mut planner = FftPlanner::new();
    let fft = planner.plan_fft_forward(samples.len());

    let mut buffer: Vec<Complex<f32>> = samples.iter().map(|s| Complex::new(s.re, s.im)).collect();

    fft.process(&mut buffer);

    buffer.iter()
        .map(|freq_bin| (freq_bin.re.powi(2) + freq_bin.im.powi(2)) as f64)
        .collect()
}
use anyhow::{bail, Error};
use log::{info, warn};
use num_complex::Complex32;
use soapysdr::{Device, Direction};
use crate::wireless::sdr::signals::fft::calculate_power_spectrum;
use crate::wireless::sdr::signals::filters::low_pass;
use crate::wireless::sdr::signals::tools::{format_frequency, linear_power_to_dbm};

pub fn scan_for_noise_floor(
    device: &Device,
    target_frequency: f64,
    bandwidth: f64,
    scan_offset: f64,
    step_size: f64,
) -> Result<f64, Error> {
    let mut noise_floor_measurements = vec![];

    // Generate stepped frequencies
    for freq in float_range(
        target_frequency - scan_offset,
        target_frequency + scan_offset,
        step_size,
    ) {
        // Avoid the target frequency itself
        if (freq - target_frequency).abs() < step_size {
            continue;
        }
        
        // Estimate noise floor at the current frequency
        match estimate_noise_floor_of_frequency(device.clone(), freq, bandwidth) {
            Ok(noise_floor) => {
                if (noise_floor > 0.0) {
                    info!("NOISE FLOOR OF {}: {}", format_frequency(freq), linear_power_to_dbm(noise_floor));
                    noise_floor_measurements.push(noise_floor);
                }
            }
            Err(e) => {
                warn!("Failed to estimate noise floor at {}: {}", freq, e);
            }
        }
    }

    // Calculate the overall noise floor (e.g., take the lowest or average value)
    if noise_floor_measurements.is_empty() {
        bail!("No noise floor found for target frequency");
    }
    let noise_floor = noise_floor_measurements.iter().cloned().fold(f64::INFINITY, f64::min);

    Ok(noise_floor)
}

pub fn estimate_noise_floor_of_frequency(device: Device, frequency: f64, bandwidth: f64) -> Result<f64, Error> {
    // Device parameters. TODO use a "tune" function.
    let sample_rate = 2_000_000.0; // TODO TODO
    device.set_sample_rate(Direction::Rx, 0, sample_rate)?;
    device.set_gain(Direction::Rx, 0, 20.0)?; // TODO TODO
    device.set_frequency(Direction::Rx, 0, frequency, "")?;

    // Receive stream and buffer.
    let mut rx_stream = device.rx_stream::<Complex32>(&[0])?;
    let mut buffer = vec![Complex32::new(0.0, 0.0); 8192];
    rx_stream.activate(None)?;

    // Capture a batch of IQ samples.
    let samples = rx_stream.read(&mut [&mut buffer], 1_000_000)?;

    // Apply low pass filter.
    let cutoff_freq = (bandwidth / 2.0) as f32;; // TODO
    let filtered_samples = low_pass(
        &buffer[..samples],
        cutoff_freq,
        1.0
    );

    // FFT.
    let power_spectrum = calculate_power_spectrum(&filtered_samples);

    Ok(estimate_noise_floor_of_power_spectrum(&power_spectrum))
}

pub fn estimate_noise_floor_of_power_spectrum(power_spectrum: &[f64]) -> f64 {
    let num_bins = power_spectrum.len();

    // Normalize power spectrum by FFT size
    let fft_size = power_spectrum.len() as f64;
    let normalized_power_spectrum: Vec<f64> = power_spectrum
        .iter()
        .map(|&power| power / fft_size) // Normalize by FFT size
        .collect();

    let filtered_bins = normalized_power_spectrum[1..num_bins / 2].to_vec(); // Exclude DC

    // Fallback if no bins pass filtering
    if filtered_bins.is_empty() {
        return normalized_power_spectrum.iter().sum::<f64>() / fft_size;
    }

    // Return the average of the noise bins (linear power)
    filtered_bins.iter().sum::<f64>() / filtered_bins.len() as f64
}

fn float_range(start: f64, end: f64, step: f64) -> impl Iterator<Item = f64> {
    (0..)
        .map(move |i| start + i as f64 * step)
        .take_while(move |&x| x <= end)
}
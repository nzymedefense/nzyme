use log::info;
use num_complex::Complex32;
use soapysdr::{Device, Direction};
use crate::wireless::sdr::signals::fft::calculate_power_spectrum;
use crate::wireless::sdr::signals::noisefloor::scan_for_noise_floor;
use crate::wireless::sdr::signals::tools::{dbm_to_linear_power, format_frequency, linear_power_to_dbm};

#[repr(C)]
pub enum SoapySDRLogLevel {
    Fatal = 1,
    Critical = 2,
    Error = 3,
    Warning = 4,
    Notice = 5,
    Info = 6,
    Debug = 7,
    Trace = 8,
}

extern "C" {
    fn SoapySDR_setLogLevel(log_level: SoapySDRLogLevel);
}

pub struct Capture {
}

impl Capture {

    pub fn run(&mut self) {
        info!("Starting SDR capture on XXXXXX");

        soapysdr::configure_logging();
        unsafe {
            SoapySDR_setLogLevel(SoapySDRLogLevel::Error);
        }

        let device = Device::new("driver=rtlsdr").unwrap();

        // TODO "tune function", find all usages, also set gain

        //let target_frequency = 462_562_500.0;
        let target_frequency = 856_387_000.0;
        //let target_frequency = 133_050_000.0;
        let bandwidth = 20_000.0; // 20 kHz
        let scan_offset = 2_000_000.0;
        let step_size = 100_000.0;

        // TODO get smarter here. try to filter out top X, which are likely busy.
        let floor = scan_for_noise_floor(&device, target_frequency, bandwidth, scan_offset, step_size).unwrap();
        info!("NOISE FLOOR: {} {}dbm", floor, linear_power_to_dbm(floor));

        let sample_rate = 2_000_000.0; // TODO TODO
        device.set_sample_rate(Direction::Rx, 0, sample_rate).unwrap();
        device.set_gain(Direction::Rx, 0, 20.0).unwrap(); // TODO TODO
        device.set_frequency(Direction::Rx, 0, target_frequency, "").unwrap();
        device.set_bandwidth(Direction::Rx, 0, bandwidth).unwrap();

        let threshold_db = 20.0;

        let mut rx_stream = device.rx_stream::<Complex32>(&[0]).unwrap();
        let mut buffer = vec![Complex32::new(0.0, 0.0); 4096];
        rx_stream.activate(None).unwrap();

        let fft_size = buffer.len() as f64;

        // TODO draw chart

        // Monitor frequency. TODO update noise floor regularly.
        loop {
            // Receive stream and buffer.
            let samples = rx_stream.read(&mut [&mut buffer], 1_000_000).unwrap();

            // Calculate power spectrum (assume in linear power)
            let fft_size = buffer.len() as f64;
            let power_spectrum: Vec<f64> = calculate_power_spectrum(&buffer[..samples])
                .iter()
                .map(|&power| power / fft_size) // Normalize by FFT size
                .collect();

            // Calculate detection threshold in linear power
            let detection_threshold = floor * (1.0 + dbm_to_linear_power(threshold_db / 10.0));

            if Self::detect_transmission(&power_spectrum, floor, detection_threshold) {
                info!("Transmission detected.");
            }
        }
    }

    fn detect_transmission(power_spectrum: &[f64], floor: f64, detection_threshold: f64) -> bool {
        // Count threshold crossings
        let threshold_crossings = power_spectrum
            .iter()
            .filter(|&&power| power > detection_threshold)
            .count();

        let percentage_crossing = (threshold_crossings as f64) / (power_spectrum.len() as f64) * 100.0;

        // Calculate average power
        let average_power = power_spectrum.iter().sum::<f64>() / power_spectrum.len() as f64;
        let snr = 10.0 * (average_power / floor).log10();

        // Total band power
        let total_band_power: f64 = power_spectrum.iter().sum();
        let scaled_noise_floor = floor * power_spectrum.len() as f64;


        if percentage_crossing > 50.0 {
            info!("{}%", percentage_crossing);
            return true;
        }

        if snr > 90.0 {
            info!("SNR");
            return true;
        }

        /*if total_band_power > scaled_noise_floor * 1.5 {
            info!("TOTAL BAND");
            return true;
        }*/

        false

        // Detection criteria
        //percentage_crossing > 10.0 || snr > 10.0 || total_band_power > scaled_noise_floor * 1.5
    }

}
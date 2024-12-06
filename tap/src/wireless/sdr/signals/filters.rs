use num_complex::Complex32;
use fundsp::hacker::*;
use fundsp::buffer::{BufferMut, BufferRef};
use fundsp::wide::f32x8;

pub fn low_pass(samples: &[Complex32], cutoff_freq: f32, q: f32) -> Vec<Complex32> {
    let mut lowpass_real = (pass() | dc(cutoff_freq)) >> lowpass_q(q);
    let mut lowpass_imag = (pass() | dc(cutoff_freq)) >> lowpass_q(q);

    // Buffers (length must be a multiple of SIMD_LEN)
    const SIMD_LEN: usize = 8; // For f32x8
    let mut output = vec![f32x8::default(); SIMD_LEN];

    samples
        .iter()
        .map(|sample| {
            // Real part.
            let mut real_input_array = [0.0.into(); SIMD_LEN]; // Ensure input is properly aligned
            real_input_array[0] = sample.re.into();
            let input = BufferRef::new(&real_input_array);
            let mut output_ref = BufferMut::new(&mut output);
            lowpass_real.process(1, &input, &mut output_ref);
            let filtered_re = output[0].to_array()[0]; // Extract the first element

            // Imaginary part.
            let mut imag_input_array = [0.0.into(); SIMD_LEN]; // Ensure input is properly aligned
            imag_input_array[0] = sample.im.into();
            let input = BufferRef::new(&imag_input_array);
            let mut output_ref = BufferMut::new(&mut output);
            lowpass_imag.process(1, &input, &mut output_ref);
            let filtered_im = output[0].to_array()[0]; // Extract the first element

            Complex32::new(filtered_re, filtered_im)
        })
        .collect()
}
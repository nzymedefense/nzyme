mod connect;
mod firmware;
mod usb;

use color_eyre::Result;
use ratatui::{DefaultTerminal, Frame};
use crate::connect::connect_firmware_directory::fetch_firmware_directory;
use crate::usb::usb::detect_nzyme_usb_devices;

fn main() -> Result<()> {
    // TODO: error handling. print nicer errors, don't panic.
    let directory = fetch_firmware_directory().unwrap();

    // TODO: Discover connected peripherals
    let devices = detect_nzyme_usb_devices().unwrap();

    for device in devices {
        println!("{:?}", device);
    }

    /*color_eyre::install()?;
    let terminal = ratatui::init();
    let result = run(terminal);
    ratatui::restore();
    result*/

    Ok(())
}

fn run(mut terminal: DefaultTerminal) -> Result<()> {
    /*loop {
        terminal.draw(render)?;
        if matches!(event::read()?, Event::Key(_)) {
            break Ok(());
        }
    }*/

    Ok(())
}

fn render(frame: &mut Frame) {
    frame.render_widget("hello world", frame.area());
}
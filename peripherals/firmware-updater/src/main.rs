mod connect;
mod firmware;

use color_eyre::Result;
use ratatui::{DefaultTerminal, Frame};
use crate::connect::connect_firmware_directory::fetch_firmware_directory;

fn main() -> Result<()> {
    // TODO: error handling. print nicer errors, don't panic.
    let directory = fetch_firmware_directory().unwrap();

    // TODO: Discover connected peripherals

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
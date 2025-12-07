mod exit_codes;
mod connect;
mod firmware;
mod usb;
mod ui;
mod arguments;
mod apps;

use clap::Parser;
use crate::apps::firmware::{firmware_gui_app, flash_firmware_app};
use crate::apps::devices::list_devices_app;
use crate::arguments::{CliArguments, Command, DevicesSubcommand, FirmwareSubcommand};
use crate::exit_codes::EX_OK;

fn main() {
    let arguments = CliArguments::parse();

    match arguments.command {
        Some(Command::Firmware(fw)) => match fw.command {
            FirmwareSubcommand::Gui => {
                // $ nzyme-util firmware gui
                firmware_gui_app::run();
            }
            FirmwareSubcommand::Flash { file, serial } => {
                // $ nzyme-util firmware flash --file foo.bin
                flash_firmware_app::run(file, serial);
            }
        },

        Some(Command::Devices(dev_cmd)) => match dev_cmd.command {
            DevicesSubcommand::List => {
                // $ nzyme-util devices list
                list_devices_app::run();
            }
        },

        None => {
            // Because arg_required_else_help = true, we normally don’t get here
        }
    }

    std::process::exit(EX_OK);
}
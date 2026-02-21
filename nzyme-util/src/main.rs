mod exit_codes;
mod connect;
mod firmware;
mod usb;
mod ui;
mod arguments;
mod apps;
mod tools;

use clap::Parser;
use crate::apps::firmware::{firmware_gui_app, flash_firmware_app, verify_firmware_app};
use crate::apps::devices::list_devices_app;
use crate::arguments::{CliArguments, Command, DevicesSubcommand, FirmwareSubcommand};
use crate::exit_codes::EX_OK;

fn main() {
    let arguments = CliArguments::parse();

    match arguments.command {
        Some(Command::Firmware(fw)) => match fw.command {
            /*FirmwareSubcommand::Gui => {
                // $ nzyme-util firmware gui
                firmware_gui_app::run();
            }*/

            FirmwareSubcommand::Flash { firmware_file, serial } => {
                // $ nzyme-util firmware flash
                flash_firmware_app::run(firmware_file, serial);
            }

            FirmwareSubcommand::Verify { firmware_file, public_key_file } => {
                // $ nzyme-util firmware flash
                verify_firmware_app::run(firmware_file, public_key_file);
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
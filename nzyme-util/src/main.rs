mod exit_codes;
mod firmware;
mod usb;
mod arguments;
mod apps;
mod tools;
mod peripherals;

use clap::Parser;
use crate::apps::firmware::{flash_firmware_app, verify_firmware_app};
use crate::apps::devices::list_devices_app;
use crate::apps::release::verify_release_app;
use crate::apps::sona::{generate_sona_configuration_app, sona_test_app};

use crate::arguments::{CliArguments, Command, DevicesSubcommand, FirmwareSubcommand, ReleaseSubcommand, SonaSubcommand};
use crate::exit_codes::EX_OK;

fn main() {
    let arguments = CliArguments::parse();

    match arguments.command {
        Some(Command::Firmware(fw)) => match fw.command {
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

        Some(Command::Release(fw)) => match fw.command {
            ReleaseSubcommand::Verify { release_file, signature_file, public_key_file } => {
                // $ nzyme-util release verify
                verify_release_app::run(release_file, signature_file, public_key_file);
            }
        },

        Some(Command::Sona(fw)) => match fw.command {
            SonaSubcommand::GenerateConfig => {
                // $ nzyme-util sona generate-config
                generate_sona_configuration_app::run();
            }
            SonaSubcommand::Test => {
                // $ nzyme-util sona test
                sona_test_app::run();
            }
        },

        None => {
            // Because arg_required_else_help = true, we normally don’t get here
        }
    }

    std::process::exit(EX_OK);
}
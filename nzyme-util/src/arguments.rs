use clap::{Parser, Subcommand, Args};

#[derive(Parser, Debug)]
#[command(
    name = "nzyme-util",
    version,
    author,
    about = "Nzyme CLI Utility",

    subcommand_required = false,
    arg_required_else_help = true
)]
pub struct CliArguments {
    #[command(subcommand)]
    pub command: Option<Command>,
}

#[derive(Subcommand, Debug)]
pub enum Command {
    Firmware(FirmwareCommand),
    Devices(DevicesCommand),
}

#[derive(Args, Debug)]
pub struct FirmwareCommand {
    #[command(subcommand)]
    pub command: FirmwareSubcommand,
}

#[derive(Subcommand, Debug)]
pub enum FirmwareSubcommand {
    Gui,

    Flash {
        #[arg(long)]
        file: String,

        #[arg(long)]
        serial: String,
    },
}

#[derive(Args, Debug)]
pub struct DevicesCommand {
    #[command(subcommand)]
    pub command: DevicesSubcommand,
}

#[derive(Subcommand, Debug)]
pub enum DevicesSubcommand {
    List,
}
use anyhow::Error;
use crossterm::event;
use crossterm::event::{Event, KeyCode, KeyEventKind};
use ratatui::{DefaultTerminal, Frame};
use ratatui::layout::{Constraint, Direction, Layout, Rect};
use ratatui::text::Text;
use ratatui::widgets::{Block, Borders, Cell, Padding, Paragraph, Row, ScrollbarState, Table, TableState};
use crate::usb::nzyme_usb_device::NzymeUsbDevice;

const INFO_TEXT: &str = "(Esc) quit | (d) show connected devices | (s) show supported devices | (↑) move up | (↓) move down ";
const ITEM_HEIGHT: usize = 2;

pub struct TuiApp {
    connected_devices: Vec<NzymeUsbDevice>,
    connected_devices_table_state: TableState,
    connected_devices_scroll_state: ScrollbarState,
    connected_devices_longest_items: (u16, u16, u16, u16, u16, u16) // Order is table column order
}

/*
 * Column order:
 *
 * 0: Product (Manufacturer)
 * 1: Serial
 * 2: Firmware Version
 * 3: ID
 * 4: ACM Port
 * 5: Address
 */

impl NzymeUsbDevice {
    fn table_row(&self) -> [String; 6] {
        [
            self.product(),
            self.serial(),
            self.firmware_version(),
            self.acm_port(),
            self.id(),
            self.address()
        ]
    }

    fn product(&self) -> String {
        format!("{} ({})", self.product, self.manufacturer)
    }

    fn serial(&self) -> String {
        self.serial.clone()
    }

    fn firmware_version(&self) -> String {
        format!("v{}.{}", self.firmware_version.major, self.firmware_version.minor)
    }

    fn acm_port(&self) -> String {
        self.acm_port.clone().unwrap_or("NONE".to_string())
    }

    fn id(&self) -> String {
        format!("0x{:04X} (0x{:04X})", self.pid, self.vid)
    }

    fn address(&self) -> String {
        format!("{}:{}", self.bus, self.address)
    }

}

impl TuiApp {
    pub fn new(connected_devices: Vec<NzymeUsbDevice>) -> Self {
        let connected_devices_longest_items = constraint_len_calculator(&connected_devices);

        let cd_len = connected_devices.len();
        Self {
            connected_devices,
            connected_devices_table_state: TableState::default().with_selected(0),
            connected_devices_scroll_state: ScrollbarState::new((cd_len - 1)),
            connected_devices_longest_items
        }
    }

    pub fn run(mut self, mut terminal: DefaultTerminal) -> Result<(), Error> {
        loop {
            terminal.draw(|frame| self.draw(frame))?;

            if let Event::Key(key) = event::read()? {
                if key.kind == KeyEventKind::Press {
                    match key.code {
                        KeyCode::Char('q') => return Ok(()),
                        _ => {}
                    }
                }
            }
        }
    }

    fn draw(&mut self, frame: &mut Frame) {
        let layout = Layout::default()
            .direction(Direction::Vertical)
            .constraints(vec![
                Constraint::Percentage(100),
                Constraint::Length(3),
            ])
            .split(frame.area());

        self.render_connected_devices_table(frame, layout[0]);

        frame.render_widget(
            Paragraph::new(INFO_TEXT)
                .block(Block::new().borders(Borders::ALL).title("Commands")),
            layout[1]);
    }

    fn render_connected_devices_table(&mut self, frame: &mut Frame, area: Rect) {
        let header = ["Product", "Serial", "Firmware Version", "ACM Port", "USB ID", "USB Address"]
            .into_iter()
            .map(Cell::from)
            .collect::<Row>()
            .height(1);

        let rows = self.connected_devices.iter().enumerate().map(|(_, data)| {
            let item = data.table_row();
            item.into_iter()
                .map(|content| Cell::from(Text::from(format!("\n{content}\n"))))
                .collect::<Row>()
                .height(ITEM_HEIGHT as u16)
        });

        let widths = [
            Constraint::Length((self.connected_devices_longest_items.0) + 1),
            Constraint::Min((self.connected_devices_longest_items.1) + 1),
            Constraint::Min((self.connected_devices_longest_items.2) + 1),
            Constraint::Min((self.connected_devices_longest_items.3) + 1),
            Constraint::Min((self.connected_devices_longest_items.4) + 1),
        ];

        let t = Table::new(rows, widths, )
            .header(header)
            .block(
                Block::default()
                    .borders(Borders::ALL)
                    .padding(Padding::new(3, 3, 1, 1))
                    .title("Connected Devices")
            );

        frame.render_stateful_widget(t, area, &mut self.connected_devices_table_state);
    }
}

fn constraint_len_calculator(items: &Vec<NzymeUsbDevice>) -> (u16, u16, u16, u16, u16, u16) {
    let c0 = items.iter()
        .map(|d| d.product().len())
        .max()
        .unwrap_or(0);

    let c1 = items.iter()
        .map(|d| d.serial().len())
        .max()
        .unwrap_or(0);

    let c2 = items.iter()
        .map(|d| d.firmware_version().len())
        .max()
        .unwrap_or(0);

    let c3 = items.iter()
        .map(|d| d.acm_port().len())
        .max()
        .unwrap_or(0);

    let c4 = items.iter()
        .map(|d| d.id().len())
        .max()
        .unwrap_or(0);

    let c5 = items.iter()
        .map(|d| d.address().len())
        .max()
        .unwrap_or(0);

    #[allow(clippy::cast_possible_truncation)]
    (c0 as u16, c1 as u16, c2 as u16, c3 as u16, c4 as u16, c5 as u16)
}
use anyhow::Error;
use crossterm::event;
use crossterm::event::{Event, KeyCode, KeyEventKind};
use ratatui::{DefaultTerminal, Frame};
use ratatui::layout::{Constraint, Direction, Layout, Rect};
use ratatui::style::{Color, Modifier, Style};
use ratatui::text::{Line, Span, Text};
use ratatui::widgets::{Block, Borders, Cell, Padding, Paragraph, Row, ScrollbarState, Table, TableState};
use crate::connect::connect_firmware_directory::Peripheral;
use crate::usb::nzyme_usb_device::NzymeUsbDevice;

const INFO_TEXT: &str = "(Esc, q) quit | (d) show connected devices | (s) show supported devices | (↑) move up | (↓) move down ";
const ITEM_HEIGHT: usize = 1;

pub struct TuiApp {
    connected_devices: Vec<NzymeUsbDevice>,
    connected_devices_table_state: TableState,
    connected_devices_scroll_state: ScrollbarState,
    connected_devices_longest_items: (u16, u16, u16, u16, u16, u16), // Order is table column order
    firmware_directory: Vec<Peripheral>
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
    fn table_row(&self, firmware_directory: &Vec<Peripheral>) -> [Line; 6] {
        [
            self.product(),
            self.serial(),
            self.firmware_version(firmware_directory),
            self.acm_port(),
            self.id(),
            self.address()
        ]
    }

    fn product(&self) -> Line<'_> {
        Line::from(vec![
            Span::raw(&self.product),
            Span::raw(" ("),
            Span::raw(&self.manufacturer),
            Span::raw(")"),
        ])
    }

    fn serial(&self) -> Line<'_> {
        Line::from(self.serial.as_str())
    }

    fn firmware_version<'a>(&self, firmware_directory: &Vec<Peripheral>) -> Line<'a> {
        let outdated = self.has_outdated_firmware(firmware_directory);

        let status_span = if outdated {
            Span::styled("❢ Outdated", Style::default().fg(Color::Red))
        } else {
            Span::styled("✔ Most Recent", Style::default().fg(Color::Green))
        };

        Line::from(vec![
            Span::raw(format!("v{}.{} (", self.firmware_version.major, self.firmware_version.minor)),
            status_span,
            Span::raw(")"),
        ])
    }

    fn acm_port(&self) -> Line<'_> {
        Line::from(
            self.acm_port
                .as_deref()
                .unwrap_or("NONE")
        )
    }

    fn id(&self) -> Line<'_> {
        Line::from(format!("0x{:04X} (0x{:04X})", self.pid, self.vid))
    }

    fn address(&self) -> Line<'_> {
        Line::from(format!("{}:{}", self.bus, self.address))
    }
}

impl TuiApp {
    pub fn new(connected_devices: Vec<NzymeUsbDevice>, firmware_directory: Vec<Peripheral>)
        -> Self {

        let connected_devices_longest_items = constraint_len_calculator(
            &connected_devices, &firmware_directory
        );

        let cd_len = connected_devices.len();
        Self {
            connected_devices,
            connected_devices_table_state: TableState::default().with_selected(0),
            connected_devices_scroll_state: ScrollbarState::new((cd_len - 1)),
            connected_devices_longest_items,
            firmware_directory
        }
    }

    pub fn run(mut self, mut terminal: DefaultTerminal) -> Result<(), Error> {
        loop {
            terminal.draw(|frame| self.draw(frame))?;

            if let Event::Key(key) = event::read()? {
                if key.kind == KeyEventKind::Press {
                    match key.code {
                        KeyCode::Esc => return Ok(()),
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
        let header_style = Style::default()
            .add_modifier(Modifier::BOLD);

        let header = Row::new(
            ["Product", "Serial", "Firmware Version", "ACM Port", "USB ID", "USB Address"]
                .into_iter()
                .map(|h| Cell::from(h).style(header_style))
        ).height(1);

        let rows = self.connected_devices.iter().map(|data| {
            let item = data.table_row(&self.firmware_directory);
            item.into_iter()
                // keep styles by passing Line -> Text directly
                .map(|line| Cell::from(Text::from(line)))
                .collect::<Row>()
                .height(ITEM_HEIGHT as u16)
        });

        let widths = [
            Constraint::Length(self.connected_devices_longest_items.0 + 1),
            Constraint::Min(self.connected_devices_longest_items.1 + 1),
            Constraint::Min(self.connected_devices_longest_items.2 + 1),
            Constraint::Min(self.connected_devices_longest_items.3 + 1),
            Constraint::Min(self.connected_devices_longest_items.4 + 1),
            Constraint::Min(self.connected_devices_longest_items.5 + 1)
        ];

        let t = Table::new(rows, widths)
            .header(header)
            .block(
                Block::default()
                    .borders(Borders::ALL)
                    .padding(Padding::new(3, 3, 1, 1))
                    .title("Connected Devices"),
            );

        frame.render_stateful_widget(t, area, &mut self.connected_devices_table_state);
    }
}

fn constraint_len_calculator(
    items: &Vec<NzymeUsbDevice>,
    firmware_directory: &Vec<Peripheral>,
) -> (u16, u16, u16, u16, u16, u16) {
    let c0 = items
        .iter()
        .map(|d| d.product().width())
        .max()
        .unwrap_or(0);

    let c1 = items
        .iter()
        .map(|d| d.serial().width())
        .max()
        .unwrap_or(0);

    let c2 = items
        .iter()
        .map(|d| d.firmware_version(firmware_directory).width())
        .max()
        .unwrap_or(0);

    let c3 = items
        .iter()
        .map(|d| d.acm_port().width())
        .max()
        .unwrap_or(0);

    let c4 = items
        .iter()
        .map(|d| d.id().width())
        .max()
        .unwrap_or(0);

    let c5 = items
        .iter()
        .map(|d| d.address().width())
        .max()
        .unwrap_or(0);

    #[allow(clippy::cast_possible_truncation)]
    (c0 as u16, c1 as u16, c2 as u16, c3 as u16, c4 as u16, c5 as u16)
}
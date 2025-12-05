use anyhow::Error;
use crossterm::event;
use crossterm::event::{Event, KeyCode, KeyEventKind};
use ratatui::{DefaultTerminal, Frame};
use ratatui::layout::{Alignment, Constraint, Direction, Layout, Rect};
use ratatui::style::{Color, Modifier, Style};
use ratatui::text::{Line, Span, Text};
use ratatui::widgets::{Block, Borders, Cell, HighlightSpacing, Padding, Paragraph, Row, Scrollbar, ScrollbarOrientation, ScrollbarState, Table, TableState, Wrap};
use unicode_width::UnicodeWidthStr;
use crate::connect::connect_firmware_directory::Peripheral;
use crate::usb::bootloader::send_enter_bootloader;
use crate::usb::nzyme_usb_device::NzymeUsbDevice;

const INFO_TEXT: &str = "(Esc, q) quit | (↑) up | (↓) down | (Return) open device | (c) close device";
const ITEM_HEIGHT: usize = 1;

pub struct TuiApp {
    devices: Vec<NzymeUsbDevice>,
    table: TableState,
    scroll_state: ScrollbarState,
    devices_longest_items: (u16, u16, u16, u16, u16, u16), // Order is table column order
    firmware_directory: Vec<Peripheral>,
    selected_device: Option<NzymeUsbDevice>,
    error: Option<String>
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
            Span::styled("❢ Update Available", Style::default().fg(Color::Red))
        } else {
            Span::styled("✔ Most Recent", Style::default().fg(Color::Green))
        };

        Line::from(vec![
            Span::raw(format!("v{}.{} (",
                              self.firmware_version.major, self.firmware_version.minor)),
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

        let connected_devices_scroll_state = match connected_devices.len() {
            0 => ScrollbarState::new(0),
            len => ScrollbarState::new(len-1)
        };

        Self {
            devices: connected_devices,
            table: TableState::default().with_selected(0),
            scroll_state: connected_devices_scroll_state,
            devices_longest_items: connected_devices_longest_items,
            firmware_directory,
            selected_device: None,
            error: None
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
                        KeyCode::Down => self.next_row(),
                        KeyCode::Up => self.previous_row(),
                        KeyCode::Enter => self.callback_enter(),
                        KeyCode::Char('c') => self.exit_device_details(),
                        KeyCode::Char('u') => self.start_upgrade(),
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

        if let Some(error) = &self.error {
            // We have an error to show.
            self.render_error(frame, layout[0], error.clone());
        } else {
            if let Some(selected_device) = &self.selected_device {
                // Device selected. Render upgrade overview.
                self.render_upgrade_overview(frame, layout[0], selected_device.clone());
            } else {
                // No device selected. Render table.
                if self.devices.is_empty() {
                    self.render_no_devices_connected_warning(frame, layout[0]);
                } else {
                    self.render_connected_devices_table(frame, layout[0]);
                }
            }
        }

        self.render_footer(frame, layout[1]);
    }

    pub fn next_row(&mut self) {
        let i = match self.table.selected() {
            Some(i) => {
                if i >= self.devices.len() - 1 {
                    0
                } else {
                    i + 1
                }
            }
            None => 0,
        };
        self.table.select(Some(i));
        self.scroll_state = self.scroll_state.position(i * ITEM_HEIGHT);
    }

    pub fn previous_row(&mut self) {
        let i = match self.table.selected() {
            Some(i) => {
                if i == 0 {
                    self.devices.len() - 1
                } else {
                    i - 1
                }
            }
            None => 0,
        };
        self.table.select(Some(i));
        self.scroll_state = self.scroll_state.position(i * ITEM_HEIGHT);
    }

    pub fn callback_enter(&mut self) {
        // A) Clear current error.
        if self.error.is_some() {
            self.error = None;
            return;
        }

        // ... or ...

        // B) Select current row.
        let i = self.table.selected().unwrap_or_else(|| 0);

        match self.devices.get(i) {
            Some(device) => self.selected_device = Some(device.clone()),
            None => {
                eprintln!("Selected device not found");
                return
            }
        }
    }

    pub fn exit_device_details(&mut self) {
        self.selected_device = None
    }

    fn render_connected_devices_table(&mut self, frame: &mut Frame, area: Rect) {
        let header_style = Style::default()
            .add_modifier(Modifier::BOLD);

        let selected_row_style = Style::default()
            .add_modifier(Modifier::BOLD);

        let header = Row::new(
            ["Product", "Serial", "Firmware Version", "ACM Port", "USB ID", "USB Address"]
                .into_iter()
                .map(|h| Cell::from(h).style(header_style))
        ).height(1);

        let rows = self.devices.iter().map(|data| {
            let item = data.table_row(&self.firmware_directory);
            item.into_iter()
                // keep styles by passing Line -> Text directly
                .map(|line| Cell::from(Text::from(line)))
                .collect::<Row>()
                .height(ITEM_HEIGHT as u16)
        });

        let widths = [
            Constraint::Length(self.devices_longest_items.0 + 1),
            Constraint::Min(self.devices_longest_items.1 + 1),
            Constraint::Min(self.devices_longest_items.2 + 1),
            Constraint::Min(self.devices_longest_items.3 + 1),
            Constraint::Min(self.devices_longest_items.4 + 1),
            Constraint::Min(self.devices_longest_items.5 + 1)
        ];

        let t = Table::new(rows, widths)
            .header(header)
            .row_highlight_style(selected_row_style)
            .highlight_symbol("❯ ")
            .highlight_spacing(HighlightSpacing::Always)
            .block(
                Block::default()
                    .borders(Borders::ALL)
                    .padding(Padding::new(3, 3, 1, 1))
                    .title(" Connected Devices "),
            );

        frame.render_stateful_widget(t, area, &mut self.table);
    }

    fn render_no_devices_connected_warning(&mut self, frame: &mut Frame, area: Rect) {
        let warning_text = Text::from(Line::from(
            Span::styled(
                "⚠  No Nzyme USB devices connected.",
                Style::default()
                    .fg(Color::Yellow)
                    .add_modifier(Modifier::BOLD),
            ),
        ));

        let warning_box = Paragraph::new(warning_text)
            .alignment(Alignment::Center)
            .wrap(Wrap { trim: false })
            .block(
                Block::default()
                    .borders(Borders::ALL)
                    .padding(Padding::top(3)),
            );

        frame.render_widget(warning_box, area);
    }

    fn render_error(&mut self, frame: &mut Frame, area: Rect, error: String) {
        let warning_text = Text::from(vec![
            // First line: error message
            Line::from(Span::styled(
                format!("⚠ Error: {}", error),
                Style::default()
                    .fg(Color::Yellow)
                    .add_modifier(Modifier::BOLD),
            )),

            // Blank line for spacing
            Line::raw(""),

            // Second line: fake button / instruction
            Line::from(Span::styled(
                "[ Press Enter to close ]",
                Style::default()
                    .fg(Color::Cyan)
                    .add_modifier(Modifier::BOLD),
            )),
        ]);

        let warning_box = Paragraph::new(warning_text)
            .alignment(Alignment::Center)
            .wrap(Wrap { trim: false })
            .block(
                Block::default()
                    .borders(Borders::ALL)
                    .padding(Padding::top(2))
                    .padding(Padding::bottom(1)),
            );

        frame.render_widget(warning_box, area);
    }

    fn render_upgrade_overview(&mut self, frame: &mut Frame, area: Rect, device: NzymeUsbDevice) {
        let firmware = device.most_recent_firmware_release_available(&self.firmware_directory);
        let device_outdated = device.has_outdated_firmware(&self.firmware_directory);

        let block = Block::default()
            .borders(Borders::ALL)
            .title(format!(" Upgrade Device {} (USB {}:{}) ",
                           device.product, device.bus, device.address));
        frame.render_widget(block.clone(), area);

        let inner = block.inner(area);

        let chunks = Layout::default()
            .direction(Direction::Vertical)
            .constraints([
                Constraint::Length(4),
                Constraint::Length(1),
                Constraint::Length(1),
                Constraint::Min(1),
            ])
            .split(inner);

        let upgrade_action_line = if device_outdated {
            Line::from(
                Span::styled(
                    "Press (u) to start upgrade.",
                    Style::default()
                        .fg(Color::Green)
                        .add_modifier(Modifier::BOLD),
                )
            )
        } else {
            Line::from(
                Span::styled(
                    "No upgrade available.",
                    Style::default()
                        .fg(Color::Red)
                        .add_modifier(Modifier::BOLD),
                )
            )
        };

        let latest_version = match &firmware {
            Some(firmware) => format!("v{}.{}", firmware.version.major, firmware.version.minor),
            None => "No firmware available".to_string()
        };

        let header_text = Text::from(vec![
            Line::from(format!("Current version: v{}.{}",
                               device.firmware_version.major, device.firmware_version.minor)),
            Line::from(format!("Latest version: {}", latest_version)),
            Line::from(""),
            upgrade_action_line
        ]);

        let header = Paragraph::new(header_text);
        frame.render_widget(header, chunks[0]);

        if device_outdated && let Some(fw) = firmware {
            let notes_body = Paragraph::new(fw.release_notes)
                .wrap(Wrap { trim: false });

            frame.render_widget(Paragraph::new("Release Notes:"), chunks[2]);
            frame.render_widget(notes_body, chunks[3]);
        }  else {
            frame.render_widget(Line::from(""), chunks[2]);
            frame.render_widget(Line::from(""), chunks[3]);
        }
    }

    fn start_upgrade(&mut self) {
        let device = match self.selected_device.clone() {
            Some(device) => device,
            None => {
                // Don't react on upgrade button press when not in device details.
                return;
            }
        };

        let acm_port = match device.acm_port {
            Some(port) => port,
            None => {
                eprintln!("Device [{:?}] exposes no ACM port.", device);
                return;
            }
        };

        if let Err(e) = send_enter_bootloader(acm_port) {
            self.error = Some("Could not send command to enter bootloader. Make sure the device is \
                not in use and stop the `nzyme-tap` service.".to_string());
        }
    }

    fn render_footer(&self, frame: &mut Frame, area: Rect) {
        let version = format!("Nzyme Firmware Updater v{}", env!("CARGO_PKG_VERSION"));

        // Inner width. (excluding borders)
        let inner_width = area.width.saturating_sub(2) as usize;

        // Compute display widths.
        let info_width = UnicodeWidthStr::width(INFO_TEXT);
        let version_width = UnicodeWidthStr::width(version.as_str());

        /*
         * We add:
         *   +1 leading space before INFO_TEXT
         *   +1 trailing space after version
         *
         */
        let total_content_width = 1 + info_width + 1 + version_width + 1;

        let padding = inner_width.saturating_sub(total_content_width);

        let line = Line::from(vec![
            Span::raw(" "),
            Span::raw(INFO_TEXT),
            Span::raw(" ".repeat(padding)),
            Span::raw(" "),
            Span::raw(version),
            Span::raw(" "),
        ]);

        let widget = Paragraph::new(line)
            .block(
                Block::new()
                    .borders(Borders::ALL)
                    .title(" Commands "),
            );

        frame.render_widget(widget, area);
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
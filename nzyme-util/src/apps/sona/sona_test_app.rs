use std::collections::{HashMap, HashSet, VecDeque};
use std::io::{self, Read};
use std::sync::atomic::{AtomicBool, Ordering};
use std::sync::{Arc, Mutex, MutexGuard};
use std::thread;
use std::time::{Duration, Instant};

use ratatui::backend::CrosstermBackend;
use ratatui::crossterm::{
    cursor::{Hide, Show},
    event::{self, Event, KeyCode, KeyEventKind, KeyModifiers},
    execute,
    terminal::{disable_raw_mode, enable_raw_mode, EnterAlternateScreen, LeaveAlternateScreen},
};
use ratatui::layout::{Constraint, Layout, Rect};
use ratatui::style::{Color, Modifier, Style};
use ratatui::text::{Line, Span};
use ratatui::widgets::{Block, BorderType, Borders, Cell, Paragraph, Row, Table, TableState, Wrap};
use ratatui::{Frame, Terminal};

use crate::exit_codes::{EX_PERMISSION_DENIED, EX_UNAVAILABLE};
use crate::peripherals::sona::framer::{SonaFramer, SonaFramerError};
use crate::peripherals::sona::metrics::SonaMetrics;
use crate::peripherals::sona::protocol::{parse_message, send_set_frequency, SonaMessage};
use crate::peripherals::sona::supported_frequencies::{getSonaSupportedFrequencies, SonaVersion};
use crate::usb::usb::{detect_nzyme_usb_devices, SONA_WIFI_PID};

const BAUD: u32 = 115_200;
const MAX_ACC: usize = 8192;

const SETTLE: Duration = Duration::from_millis(80); // discard right after a channel switch
const DWELL: Duration = Duration::from_millis(1000); // hop time / measurement window per channel
const PAUSED_DWELL: Duration = Duration::from_millis(400);
const UI_TICK: Duration = Duration::from_millis(120);

const ACTIVE_RECENT: Duration = Duration::from_secs(4); // "lit up" window in the heatmap

struct SonaSensor {
    serial: String,
    acm_port: String,
    bus: String,
    address: String,
}

#[derive(Clone)]
enum SensorStatus {
    Connecting,
    Running,
    Failed(String),
}

#[derive(Clone)]
struct ChannelStat {
    freq_mhz: u16,
    frames: u64,
    last_seen: Option<Instant>,
    rssi_min: Option<f32>,
    rssi_max: Option<f32>,
    last_rssi: Option<f32>,
}

#[derive(Clone)]
struct SensorState {
    serial: String,
    bus: String,
    address: String,
    acm_port: String,
    status: SensorStatus,
    commanded_freq: Option<u16>,
    hops: u64,
    channels: Vec<ChannelStat>,
    metrics: Option<SonaMetrics>,
    metrics_at: Option<Instant>,
    total_frames: u64,
    decode_errors: u64,
    last_frame_at: Option<Instant>,
}

impl SensorState {
    fn new(s: &SonaSensor, freqs: &[u16]) -> Self {
        Self {
            serial: s.serial.clone(),
            bus: s.bus.clone(),
            address: s.address.clone(),
            acm_port: s.acm_port.clone(),
            status: SensorStatus::Connecting,
            commanded_freq: None,
            hops: 0,
            channels: freqs
                .iter()
                .map(|&f| ChannelStat {
                    freq_mhz: f,
                    frames: 0,
                    last_seen: None,
                    rssi_min: None,
                    rssi_max: None,
                    last_rssi: None,
                })
                .collect(),
            metrics: None,
            metrics_at: None,
            total_frames: 0,
            decode_errors: 0,
            last_frame_at: None,
        }
    }
}

fn lock<T>(m: &Mutex<T>) -> MutexGuard<'_, T> {
    m.lock().unwrap_or_else(|e| e.into_inner())
}

pub fn run() {
    let sensors = match discover_sona_sensors() {
        Ok(s) => s,
        Err(e) => {
            eprintln!("[x] Could not enumerate USB devices ({}). Check permissions.", e);
            std::process::exit(EX_PERMISSION_DENIED);
        }
    };

    if sensors.is_empty() {
        eprintln!("[!] No Sona sensors found.");
        std::process::exit(EX_UNAVAILABLE);
    }

    let frequencies =
        dedup_preserve(getSonaSupportedFrequencies(SonaVersion::One).into_iter().map(|f| f.frequency));

    let shutdown = Arc::new(AtomicBool::new(false));
    let paused = Arc::new(AtomicBool::new(false));

    let mut states: Vec<Arc<Mutex<SensorState>>> = Vec::new();
    let mut handles = Vec::new();

    for sensor in sensors {
        let state = Arc::new(Mutex::new(SensorState::new(&sensor, &frequencies)));
        states.push(state.clone());

        let freqs = frequencies.clone();
        let sd = shutdown.clone();
        let pz = paused.clone();
        handles.push(thread::spawn(move || capture_thread(state, sensor, freqs, sd, pz)));
    }

    if let Err(e) = run_ui(&states, &paused) {
        eprintln!("UI error: {}", e);
    }

    shutdown.store(true, Ordering::Relaxed);
    for h in handles {
        let _ = h.join();
    }
}

fn discover_sona_sensors() -> Result<Vec<SonaSensor>, Box<dyn std::error::Error>> {
    let devices = detect_nzyme_usb_devices()?;

    let mut sensors = Vec::new();
    for d in devices {
        if d.pid != SONA_WIFI_PID {
            continue;
        }
        let acm_port = match d.acm_port {
            Some(p) => p,
            None => continue,
        };
        sensors.push(SonaSensor {
            serial: d.serial,
            acm_port,
            bus: d.bus.to_string(),
            address: d.address.to_string(),
        });
    }

    sensors.sort_by(|a, b| a.serial.cmp(&b.serial).then(a.acm_port.cmp(&b.acm_port)));
    Ok(sensors)
}

fn capture_thread(
    state: Arc<Mutex<SensorState>>,
    sensor: SonaSensor,
    frequencies: Vec<u16>,
    shutdown: Arc<AtomicBool>,
    paused: Arc<AtomicBool>,
) {
    let freq_index: HashMap<u16, usize> =
        frequencies.iter().enumerate().map(|(i, &f)| (f, i)).collect();

    let mut port = match serialport::new(sensor.acm_port.clone(), BAUD)
        .timeout(Duration::from_millis(50))
        .open()
    {
        Ok(mut p) => {
            let _ = p.write_data_terminal_ready(true);
            p
        }
        Err(e) => {
            lock(&state).status = SensorStatus::Failed(format!("open: {}", e));
            return;
        }
    };

    // Sync to the COBS stream: let in-flight bytes settle, then drop the buffer.
    thread::sleep(Duration::from_millis(150));
    let _ = port.clear(serialport::ClearBuffer::Input);
    lock(&state).status = SensorStatus::Running;

    let mut framer = SonaFramer::new(MAX_ACC);
    let mut idx = 0usize;

    while !shutdown.load(Ordering::Relaxed) {
        let freq = frequencies[idx];
        let is_paused = paused.load(Ordering::Relaxed);

        if !is_paused {
            if send_set_frequency(&mut *port, freq).is_ok() {
                let mut s = lock(&state);
                s.commanded_freq = Some(freq);
                s.hops += 1;
            }
            let mut junk = 0u64;
            let _ = read_window(&mut port, &mut framer, SETTLE, &mut junk, |_| {});
        }

        // Dwell: accumulate locally, then merge under one lock.
        let mut per_freq: HashMap<u16, (u64, Option<f32>, Option<f32>, Option<f32>)> = HashMap::new();
        let mut frames = 0u64;
        let mut new_metrics: Option<SonaMetrics> = None;
        let mut win_errs = 0u64;

        let dwell = if is_paused { PAUSED_DWELL } else { DWELL };
        let read_err = read_window(&mut port, &mut framer, dwell, &mut win_errs, |msg| match msg {
            SonaMessage::Dot11 { header, .. } => {
                frames += 1;
                let e = per_freq.entry(header.freq_mhz).or_insert((0, None, None, None));
                e.0 += 1;
                if let Some(r) = header.rssi_dbm {
                    e.1 = Some(e.1.map_or(r, |m| m.min(r)));
                    e.2 = Some(e.2.map_or(r, |m| m.max(r)));
                    e.3 = Some(r);
                }
            }
            SonaMessage::Metrics(m) => new_metrics = Some(m),
            SonaMessage::Unknown(_) => {}
        });

        let now = Instant::now();
        let commanded_idx = freq_index.get(&freq).copied();
        {
            let mut s = lock(&state);
            s.total_frames += frames;
            s.decode_errors += win_errs;
            if frames > 0 {
                s.last_frame_at = Some(now);
            }
            if let Some(m) = new_metrics {
                s.metrics = Some(m);
                s.metrics_at = Some(now);
            }
            for (f, (cnt, rmin, rmax, last)) in per_freq {
                // Credit by reported freq and fall back to the commanded channel for
                // any frame whose freq isn't in our list.
                if let Some(i) = freq_index.get(&f).copied().or(commanded_idx) {
                    let ch = &mut s.channels[i];
                    ch.frames += cnt;
                    ch.last_seen = Some(now);
                    if let Some(r) = rmin {
                        ch.rssi_min = Some(ch.rssi_min.map_or(r, |m| m.min(r)));
                    }
                    if let Some(r) = rmax {
                        ch.rssi_max = Some(ch.rssi_max.map_or(r, |m| m.max(r)));
                    }
                    if last.is_some() {
                        ch.last_rssi = last;
                    }
                }
            }
        }

        if let Some(e) = read_err {
            lock(&state).status = SensorStatus::Failed(format!("read: {}", e));
            return;
        }

        idx = (idx + 1) % frequencies.len();
    }
}

fn read_window<F: FnMut(SonaMessage)>(
    port: &mut Box<dyn serialport::SerialPort>,
    framer: &mut SonaFramer,
    duration: Duration,
    decode_errors: &mut u64,
    mut on_message: F,
) -> Option<String> {
    let mut chunk = [0u8; 512];
    let deadline = Instant::now() + duration;
    while Instant::now() < deadline {
        match port.read(&mut chunk) {
            Ok(n) if n > 0 => {
                if let Err(SonaFramerError::Overflow) = framer.push(&chunk[..n]) {
                    continue;
                }
                framer.drain(|frame| match frame {
                    Ok(decoded) => match parse_message(&decoded) {
                        Ok(msg) => on_message(msg),
                        Err(_) => *decode_errors += 1,
                    },
                    Err(_) => *decode_errors += 1,
                });
            }
            Ok(_) => {}
            Err(ref e) if e.kind() == std::io::ErrorKind::TimedOut => {}
            Err(e) => return Some(e.to_string()),
        }
    }
    None
}

fn run_ui(states: &[Arc<Mutex<SensorState>>], paused: &Arc<AtomicBool>) -> io::Result<()> {
    enable_raw_mode()?;
    let mut stdout = io::stdout();
    execute!(stdout, EnterAlternateScreen, Hide)?;

    // Restore the terminal even if something panics.
    let prev = std::panic::take_hook();
    std::panic::set_hook(Box::new(move |info| {
        let _ = disable_raw_mode();
        let _ = execute!(io::stdout(), LeaveAlternateScreen, Show);
        prev(info);
    }));

    let backend = CrosstermBackend::new(stdout);
    let mut terminal = Terminal::new(backend)?;

    let n = states.len();
    let mut selected = 0usize;
    let mut hist: Vec<VecDeque<u64>> = vec![VecDeque::with_capacity(64); n];
    let mut last_total = vec![0u64; n];
    let mut last_sample = Instant::now();
    let start = Instant::now();

    let res = (|| -> io::Result<()> {
        loop {
            let now = Instant::now();
            let snaps: Vec<SensorState> = states.iter().map(|s| lock(s).clone()).collect();

            // Sample frames/sec on a fixed cadence (independent of key events).
            if now.duration_since(last_sample) >= UI_TICK {
                let elapsed = now.duration_since(last_sample).as_secs_f64().max(1e-3);
                for i in 0..n {
                    let d = snaps[i].total_frames.saturating_sub(last_total[i]);
                    let rate = (d as f64 / elapsed).round() as u64;
                    let h = &mut hist[i];
                    if h.len() >= 60 {
                        h.pop_front();
                    }
                    h.push_back(rate);
                    last_total[i] = snaps[i].total_frames;
                }
                last_sample = now;
            }

            let is_paused = paused.load(Ordering::Relaxed);
            terminal.draw(|f| ui(f, &snaps, selected, &hist, is_paused, start))?;

            if event::poll(UI_TICK)? {
                if let Event::Key(k) = event::read()? {
                    if k.kind == KeyEventKind::Press {
                        match k.code {
                            KeyCode::Char('q') | KeyCode::Esc => break,
                            KeyCode::Char('c') if k.modifiers.contains(KeyModifiers::CONTROL) => break,
                            KeyCode::Up | KeyCode::Char('k') => selected = selected.saturating_sub(1),
                            KeyCode::Down | KeyCode::Char('j') => {
                                if selected + 1 < n {
                                    selected += 1;
                                }
                            }
                            KeyCode::Char(' ') => {
                                let v = paused.load(Ordering::Relaxed);
                                paused.store(!v, Ordering::Relaxed);
                            }
                            _ => {}
                        }
                    }
                }
            }
        }
        Ok(())
    })();

    disable_raw_mode()?;
    execute!(terminal.backend_mut(), LeaveAlternateScreen, Show)?;
    terminal.show_cursor()?;
    res
}

fn ui(f: &mut Frame, snaps: &[SensorState], selected: usize, hist: &[VecDeque<u64>], paused: bool, start: Instant) {
    let now = Instant::now();
    let chunks = Layout::vertical([Constraint::Length(3), Constraint::Min(0), Constraint::Length(2)])
        .split(f.area());

    render_header(f, chunks[0], snaps, paused, start, now);

    let body = Layout::horizontal([Constraint::Percentage(44), Constraint::Percentage(56)]).split(chunks[1]);

    let left = Layout::vertical([Constraint::Length(snaps.len() as u16 + 3), Constraint::Min(0)]).split(body[0]);
    render_list(f, left[0], snaps, selected, hist);

    let sweep_secs = snaps.first().map(|s| s.channels.len() as u64).unwrap_or(0);
    let elapsed = now.duration_since(start).as_secs();
    render_checklist(f, left[1], snaps, elapsed, sweep_secs);

    render_detail(f, body[1], snaps, selected, hist.get(selected), paused, now);

    render_footer(f, chunks[2]);
}

fn render_header(f: &mut Frame, area: Rect, snaps: &[SensorState], paused: bool, start: Instant, now: Instant) {
    let live = snaps.iter().filter(|s| matches!(s.status, SensorStatus::Running)).count();
    let total_frames: u64 = snaps.iter().map(|s| s.total_frames).sum();
    let elapsed = now.duration_since(start).as_secs();

    let mut spans = vec![
        Span::styled(
            " Nzyme Sona Test Routine ",
            Style::default().fg(Color::Black).bg(Color::Cyan).add_modifier(Modifier::BOLD),
        ),
        Span::raw("   "),
        Span::styled(format!("{} sensors", snaps.len()), Style::default().fg(Color::White)),
        Span::raw("   "),
        Span::styled(format!("{} live", live), Style::default().fg(Color::Green)),
        Span::raw("   "),
        Span::styled(format!("{} frames", total_frames), Style::default().fg(Color::Gray)),
        Span::raw("   "),
        Span::styled(format!("{}s", elapsed), Style::default().fg(Color::DarkGray)),
    ];
    if paused {
        spans.push(Span::raw("   "));
        spans.push(Span::styled(
            " PAUSED ",
            Style::default().fg(Color::Black).bg(Color::Yellow).add_modifier(Modifier::BOLD),
        ));
    }

    let p = Paragraph::new(Line::from(spans)).block(Block::default().borders(Borders::BOTTOM));
    f.render_widget(p, area);
}

fn render_list(f: &mut Frame, area: Rect, snaps: &[SensorState], selected: usize, hist: &[VecDeque<u64>]) {
    let header = Row::new(["", "SERIAL", "CH", "FPS", "TEMP", "FW"].iter().map(|h| {
        Cell::from(Span::styled(*h, Style::default().fg(Color::DarkGray).add_modifier(Modifier::BOLD)))
    }))
        .height(1);

    let rows: Vec<Row> = snaps
        .iter()
        .enumerate()
        .map(|(i, s)| {
            let (dot, dotc) = match &s.status {
                SensorStatus::Running => ("●", Color::Green),
                SensorStatus::Connecting => ("◐", Color::Yellow),
                SensorStatus::Failed(_) => ("●", Color::Red),
            };
            let ch = s.commanded_freq.map(|f| f.to_string()).unwrap_or_else(|| "—".into());
            let rate = hist.get(i).and_then(|h| h.back()).copied().unwrap_or(0);
            let spk = spark(&slice_tail(hist.get(i), 8));
            let fps = format!("{:>3} {}", rate, spk);
            let (temp, fw) = match &s.metrics {
                Some(m) => (
                    format!("{:.0}°C", m.temperature_mc as f64 / 1000.0),
                    firmware_version(m.version_bcd),
                ),
                None => ("—".into(), "—".into()),
            };

            Row::new(vec![
                Cell::from(Span::styled(dot, Style::default().fg(dotc))),
                Cell::from(s.serial.clone()),
                Cell::from(Span::styled(ch, Style::default().fg(Color::Yellow).add_modifier(Modifier::BOLD))),
                Cell::from(Span::styled(fps, Style::default().fg(Color::Cyan))),
                Cell::from(temp),
                Cell::from(fw),
            ])
        })
        .collect();

    let widths = [
        Constraint::Length(1),
        Constraint::Min(10),
        Constraint::Length(5),
        Constraint::Length(13),
        Constraint::Length(6),
        Constraint::Length(4),
    ];

    let table = Table::new(rows, widths)
        .header(header)
        .block(Block::default().borders(Borders::ALL).border_type(BorderType::Rounded).title(" Boards "))
        .row_highlight_style(Style::default().bg(Color::Rgb(40, 44, 60)).add_modifier(Modifier::BOLD))
        .highlight_symbol("▸ ");

    let mut st = TableState::default();
    st.select(Some(selected));
    f.render_stateful_widget(table, area, &mut st);
}

fn render_detail(
    f: &mut Frame,
    area: Rect,
    snaps: &[SensorState],
    selected: usize,
    hist: Option<&VecDeque<u64>>,
    paused: bool,
    now: Instant,
) {
    let parts = Layout::vertical([Constraint::Length(10), Constraint::Length(3), Constraint::Min(0)]).split(area);

    if let Some(s) = snaps.get(selected) {
        render_detail_metrics(f, parts[0], s, paused, now);
    }

    let spk = spark(&slice_tail(hist, parts[1].width.saturating_sub(2) as usize));
    let fp = Paragraph::new(Line::from(Span::styled(spk, Style::default().fg(Color::Cyan))))
        .block(Block::default().borders(Borders::ALL).title(" frames/s "));
    f.render_widget(fp, parts[1]);

    // Two channel views: aggregate (all sensors) on top, selected sensor below.
    let maps = Layout::vertical([Constraint::Percentage(50), Constraint::Percentage(50)]).split(parts[2]);

    let (agg, agg_current) = aggregate_channels(snaps);
    render_heatmap(f, maps[0], &agg, &agg_current, " Channel Activity · all sensors ", true, now);

    if let Some(s) = snaps.get(selected) {
        let mut current = HashSet::new();
        if let Some(fq) = s.commanded_freq {
            current.insert(fq);
        }
        let title = format!(" Channel Activity · {} ", s.serial);
        render_heatmap(f, maps[1], &s.channels, &current, &title, false, now);
    }
}

fn render_detail_metrics(f: &mut Frame, area: Rect, s: &SensorState, paused: bool, now: Instant) {
    let dim = Style::default().fg(Color::DarkGray);
    let title = format!(" {} · {} · bus {}:{} ", s.serial, s.acm_port, s.bus, s.address);

    let mut lines: Vec<Line> = Vec::new();

    if let SensorStatus::Failed(e) = &s.status {
        lines.push(Line::from(Span::styled(
            format!("FAILED — {}", e),
            Style::default().fg(Color::Red).add_modifier(Modifier::BOLD),
        )));
    } else {
        let (stxt, sc) = match s.status {
            SensorStatus::Running => ("RUNNING", Color::Green),
            _ => ("CONNECTING", Color::Yellow),
        };
        let (hop_txt, hop_c) = if paused {
            ("PAUSED", Color::Yellow)
        } else {
            ("hopping", Color::Green)
        };
        lines.push(Line::from(vec![
            Span::styled("status ", dim),
            Span::styled(stxt, Style::default().fg(sc).add_modifier(Modifier::BOLD)),
            Span::raw("    "),
            Span::styled(hop_txt, Style::default().fg(hop_c).add_modifier(Modifier::BOLD)),
            Span::styled(format!("  hops {}", s.hops), dim),
        ]));

        let cur = s.commanded_freq.map(|f| format!("{} MHz", f)).unwrap_or_else(|| "—".into());
        lines.push(Line::from(vec![
            Span::styled("current channel  ", dim),
            Span::styled(cur, Style::default().fg(Color::Yellow).add_modifier(Modifier::BOLD)),
        ]));

        match &s.metrics {
            Some(m) => {
                let age = s.metrics_at.map(|t| now.duration_since(t).as_secs()).unwrap_or(0);
                let reset = if m.last_reset_reason == 0 {
                    "NONE".to_string()
                } else {
                    format!("0x{:08X}", m.last_reset_reason)
                };
                lines.push(Line::from(format!(
                    "uptime {:.0}s    temp {:.1}°C    Firmware {}",
                    m.uptime_ms as f64 / 1000.0,
                    m.temperature_mc as f64 / 1000.0,
                    firmware_version(m.version_bcd)
                )));
                lines.push(Line::from(format!(
                    "queue {}    drops {}    stale {}    reset {}",
                    m.frame_queue_used, m.frame_queue_drops, m.frame_queue_stale_drops, reset
                )));
                lines.push(Line::from(Span::styled(format!("metrics {}s ago", age), Style::default().fg(Color::Cyan))));
            }
            None => {
                lines.push(Line::from(Span::styled(
                    "metrics: waiting…  (low-cadence frame, appears within a few seconds)",
                    Style::default().fg(Color::Yellow),
                )));
            }
        }

        lines.push(Line::from(Span::styled(
            format!("total frames {}    decode err {}", s.total_frames, s.decode_errors),
            dim,
        )));
    }

    let p = Paragraph::new(lines)
        .block(Block::default().borders(Borders::ALL).border_type(BorderType::Rounded).title(title));
    f.render_widget(p, area);
}

fn render_heatmap(
    f: &mut Frame,
    area: Rect,
    channels: &[ChannelStat],
    current: &HashSet<u16>,
    title: &str,
    show_legend: bool,
    now: Instant,
) {
    let mut lines: Vec<Line> = Vec::new();

    if show_legend {
        lines.push(Line::from(vec![
            Span::styled(" ▸0000 ", cur_sample_style()),
            Span::raw(" current   "),
            Span::styled(" 0000 ", Style::default().fg(Color::White).bg(Color::Rgb(0, 180, 60))),
            Span::raw(" active   "),
            Span::styled("  ·   ", Style::default().fg(Color::Rgb(90, 90, 90)).bg(Color::Rgb(28, 28, 32))),
            Span::raw(" silent  "),
            Span::styled("(no AP there — normal)", Style::default().fg(Color::DarkGray)),
        ]));
        lines.push(Line::raw(""));
    }

    for (label, lo, hi) in [("2.4 GHz", 0u16, 2999u16), ("5 GHz", 3000u16, u16::MAX)] {
        let band: Vec<&ChannelStat> =
            channels.iter().filter(|c| c.freq_mhz >= lo && c.freq_mhz <= hi).collect();
        if band.is_empty() {
            continue;
        }
        lines.push(Line::from(Span::styled(
            label,
            Style::default().fg(Color::White).add_modifier(Modifier::BOLD | Modifier::UNDERLINED),
        )));
        for chunk in band.chunks(7) {
            let mut spans = Vec::new();
            for ch in chunk {
                let is_cur = current.contains(&ch.freq_mhz);
                let cell = if is_cur {
                    format!("▸{:>4} ", ch.freq_mhz)
                } else {
                    format!(" {:>4} ", ch.freq_mhz)
                };
                spans.push(Span::styled(cell, cell_style(ch, is_cur, now)));
                spans.push(Span::raw(" "));
            }
            lines.push(Line::from(spans));
        }
        lines.push(Line::raw(""));
    }

    let p = Paragraph::new(lines).block(
        Block::default()
            .borders(Borders::ALL)
            .border_type(BorderType::Rounded)
            .title(title.to_string()),
    );
    f.render_widget(p, area);
}

fn render_footer(f: &mut Frame, area: Rect) {
    let key = Style::default().fg(Color::White).bg(Color::Rgb(50, 50, 60)).add_modifier(Modifier::BOLD);
    let p = Paragraph::new(Line::from(vec![
        Span::styled(" q ", key),
        Span::raw(" quit   "),
        Span::styled(" ↑/↓ ", key),
        Span::raw(" select board   "),
        Span::styled(" space ", key),
        Span::raw(" pause/resume hopping "),
    ]))
        .block(Block::default().borders(Borders::TOP));
    f.render_widget(p, area);
}
----------------------------------------------------------------

enum CheckState {
    Pass,
    Pending,
    Fail,
}

fn compute_checks(snaps: &[SensorState], elapsed_secs: u64, sweep_secs: u64) -> Vec<(String, CheckState)> {
    let n = snaps.len();
    let failed = snaps.iter().filter(|s| matches!(s.status, SensorStatus::Failed(_))).count();
    let running = snaps.iter().filter(|s| matches!(s.status, SensorStatus::Running)).count();
    let with_metrics = snaps.iter().filter(|s| s.metrics.is_some()).count();
    let hopping = snaps.iter().filter(|s| s.hops >= 2).count();
    let capturing = snaps.iter().filter(|s| s.total_frames > 0).count();

    // Definite as soon as enumeration is done.
    let count_state = if n > 0 && n % 4 == 0 { CheckState::Pass } else { CheckState::Fail };

    let stream_state = if failed > 0 {
        CheckState::Fail
    } else if running == n {
        CheckState::Pass
    } else {
        CheckState::Pending
    };

    let metrics_state = if with_metrics == n { CheckState::Pass } else { CheckState::Pending };

    let hop_state = if hopping == n {
        CheckState::Pass
    } else if failed > 0 {
        CheckState::Fail
    } else {
        CheckState::Pending
    };

    let capture_state = if capturing == n {
        CheckState::Pass
    } else if failed > 0 {
        CheckState::Fail
    } else {
        CheckState::Pending
    };

    let sweep_state = if sweep_secs > 0 && elapsed_secs >= sweep_secs {
        CheckState::Pass
    } else {
        CheckState::Pending
    };

    vec![
        (format!("Sensor count is a multiple of 4 (got {})", n), count_state),
        (format!("All sensors streaming ({}/{})", running, n), stream_state),
        (format!("All sensors report metrics ({}/{})", with_metrics, n), metrics_state),
        (format!("All sensors hopping channels ({}/{})", hopping, n), hop_state),
        (format!("All sensors capturing frames ({}/{})", capturing, n), capture_state),
        (format!("Completed a full sweep (~{}s)", sweep_secs), sweep_state),
    ]
}

fn render_checklist(f: &mut Frame, area: Rect, snaps: &[SensorState], elapsed_secs: u64, sweep_secs: u64) {
    let checks = compute_checks(snaps, elapsed_secs, sweep_secs);
    let passed = checks.iter().filter(|(_, st)| matches!(st, CheckState::Pass)).count();
    let all = passed == checks.len();

    let mut lines: Vec<Line> = Vec::new();

    for (label, state) in &checks {
        let (mark, mark_style, text_style) = match state {
            CheckState::Pass => (
                "✓",
                Style::default().fg(Color::Green).add_modifier(Modifier::BOLD),
                Style::default().fg(Color::Green),
            ),
            CheckState::Pending => (
                "◦",
                Style::default().fg(Color::Rgb(110, 110, 110)),
                Style::default().fg(Color::Gray),
            ),
            CheckState::Fail => (
                "✗",
                Style::default().fg(Color::Red).add_modifier(Modifier::BOLD),
                Style::default().fg(Color::Red),
            ),
        };
        lines.push(Line::from(vec![
            Span::styled(format!(" {} ", mark), mark_style),
            Span::styled(label.clone(), text_style),
        ]));
    }

    lines.push(Line::raw(""));
    if all {
        lines.push(Line::from(Span::styled(
            " ALL CHECKS PASS — adapters look healthy ",
            Style::default().fg(Color::Black).bg(Color::Green).add_modifier(Modifier::BOLD),
        )));
    } else {
        lines.push(Line::from(Span::styled(
            format!("{}/{} checks passing…", passed, checks.len()),
            Style::default().fg(Color::DarkGray),
        )));
    }
    lines.push(Line::from(Span::styled(
        "Silent channels on their own are fine — no AP there.",
        Style::default().fg(Color::DarkGray),
    )));

    let p = Paragraph::new(lines)
        .block(
            Block::default()
                .borders(Borders::ALL)
                .border_type(BorderType::Rounded)
                .title(" Self-Test Checklist "),
        )
        .wrap(Wrap { trim: true });
    f.render_widget(p, area);
}

fn cur_sample_style() -> Style {
    Style::default().fg(Color::Black).bg(Color::Yellow).add_modifier(Modifier::BOLD)
}

fn cell_style(ch: &ChannelStat, is_current: bool, now: Instant) -> Style {
    if is_current {
        return cur_sample_style();
    }
    if ch.frames == 0 {
        return Style::default().fg(Color::Rgb(90, 90, 90)).bg(Color::Rgb(28, 28, 32));
    }
    let recent = ch.last_seen.map_or(false, |t| now.duration_since(t) < ACTIVE_RECENT);
    let lvl = ((ch.frames as f64).ln_1p() / 5.0).min(1.0);
    let g = 70 + (lvl * 170.0) as u8;
    if recent {
        Style::default().fg(Color::White).bg(Color::Rgb(0, g, 40)).add_modifier(Modifier::BOLD)
    } else {
        Style::default().fg(Color::Rgb(180, 200, 180)).bg(Color::Rgb(0, (g / 2).max(40), 20))
    }
}

fn firmware_version(bcd: u16) -> String {
    let major = (bcd >> 8) & 0xFF;
    let minor = ((bcd >> 4) & 0x0F) * 10 + (bcd & 0x0F);
    format!("{}.{}", major, minor)
}

fn spark(data: &[u64]) -> String {
    const BARS: [char; 8] = ['▁', '▂', '▃', '▄', '▅', '▆', '▇', '█'];
    let max = data.iter().copied().max().unwrap_or(0).max(1);
    data.iter()
        .map(|&v| {
            if v == 0 {
                ' '
            } else {
                let idx = ((v as f64 / max as f64) * 7.0).round() as usize;
                BARS[idx.min(7)]
            }
        })
        .collect()
}

fn slice_tail(h: Option<&VecDeque<u64>>, n: usize) -> Vec<u64> {
    match h {
        Some(h) => {
            let start = h.len().saturating_sub(n);
            h.iter().skip(start).copied().collect()
        }
        None => Vec::new(),
    }
}

fn dedup_preserve<I: IntoIterator<Item = u16>>(it: I) -> Vec<u16> {
    let mut seen = HashSet::new();
    it.into_iter().filter(|f| seen.insert(*f)).collect()
}

fn aggregate_channels(snaps: &[SensorState]) -> (Vec<ChannelStat>, HashSet<u16>) {
    let mut agg: Vec<ChannelStat> = match snaps.first() {
        Some(first) => first
            .channels
            .iter()
            .map(|c| ChannelStat {
                freq_mhz: c.freq_mhz,
                frames: 0,
                last_seen: None,
                rssi_min: None,
                rssi_max: None,
                last_rssi: None,
            })
            .collect(),
        None => Vec::new(),
    };

    let idx: HashMap<u16, usize> = agg.iter().enumerate().map(|(i, c)| (c.freq_mhz, i)).collect();
    let mut current: HashSet<u16> = HashSet::new();

    for s in snaps {
        if let Some(fq) = s.commanded_freq {
            current.insert(fq);
        }
        for c in &s.channels {
            if let Some(&i) = idx.get(&c.freq_mhz) {
                agg[i].frames += c.frames;
                agg[i].last_seen = later(agg[i].last_seen, c.last_seen);
            }
        }
    }

    (agg, current)
}

fn later(a: Option<Instant>, b: Option<Instant>) -> Option<Instant> {
    match (a, b) {
        (Some(x), Some(y)) => Some(x.max(y)),
        (Some(x), None) => Some(x),
        (None, y) => y,
    }
}
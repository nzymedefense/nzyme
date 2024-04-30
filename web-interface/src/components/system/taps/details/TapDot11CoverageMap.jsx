import React from "react";

import numeral from "numeral";

const channels24 = [
  { channel: 1, freq: 2412 },
  { channel: 2, freq: 2417 },
  { channel: 3, freq: 2422 },
  { channel: 4, freq: 2427 },
  { channel: 5, freq: 2432 },
  { channel: 6, freq: 2437 },
  { channel: 7, freq: 2442 },
  { channel: 8, freq: 2447 },
  { channel: 9, freq: 2452 },
  { channel: 10, freq: 2457 },
  { channel: 11, freq: 2462 },
  { channel: 12, freq: 2467 },
  { channel: 13, freq: 2472 },
  { channel: 14, freq: 2484 }
];

const channels5 = [
  { channel: 32, freq: 5160 },
  { channel: 36, freq: 5180 },
  { channel: 40, freq: 5200 },
  { channel: 44, freq: 5220 },
  { channel: 48, freq: 5240 },
  { channel: 52, freq: 5260 },
  { channel: 56, freq: 5280 },
  { channel: 60, freq: 5300 },
  { channel: 64, freq: 5320 },
  { channel: 68, freq: 5340 },
  { channel: 96, freq: 5480 },
  { channel: 100, freq: 5500 },
  { channel: 104, freq: 5520 },
  { channel: 108, freq: 5540 },
  { channel: 112, freq: 5560 },
  { channel: 116, freq: 5580 },
  { channel: 120, freq: 5600 },
  { channel: 124, freq: 5620 },
  { channel: 128, freq: 5640 },
  { channel: 132, freq: 5660 },
  { channel: 136, freq: 5680 },
  { channel: 140, freq: 5700 },
  { channel: 144, freq: 5720 },
  { channel: 149, freq: 5745 },
  { channel: 153, freq: 5765 },
  { channel: 157, freq: 5785 },
  { channel: 161, freq: 5805 },
  { channel: 165, freq: 5825 },
  { channel: 169, freq: 5845 },
  { channel: 173, freq: 5865 },
  { channel: 177, freq: 5885 }
];

const channels6 = [
  { channel: 1, freq: 5955 },
  { channel: 2, freq: 5935 },
  { channel: 5, freq: 5975 },
  { channel: 9, freq: 5995 },
  { channel: 13, freq: 6015 },
  { channel: 17, freq: 6035 },
  { channel: 21, freq: 6055 },
  { channel: 25, freq: 6075 },
  { channel: 29, freq: 6095 },
  { channel: 33, freq: 6115 },
  { channel: 37, freq: 6135 },
  { channel: 41, freq: 6155 },
  { channel: 45, freq: 6175 },
  { channel: 49, freq: 6195 },
  { channel: 53, freq: 6215 },
  { channel: 57, freq: 6235 },
  { channel: 61, freq: 6255 },
  { channel: 65, freq: 6275 },
  { channel: 69, freq: 6295 },
  { channel: 73, freq: 6315 },
  { channel: 77, freq: 6335 },
  { channel: 81, freq: 6355 },
  { channel: 85, freq: 6375 },
  { channel: 89, freq: 6395 },
  { channel: 93, freq: 6415 },
  { channel: 97, freq: 6435 },
  { channel: 101, freq: 6455 },
  { channel: 105, freq: 6475 },
  { channel: 109, freq: 6495 },
  { channel: 113, freq: 6515 },
  { channel: 117, freq: 6535 },
  { channel: 121, freq: 6555 },
  { channel: 125, freq: 6575 },
  { channel: 129, freq: 6595 },
  { channel: 133, freq: 6615 },
  { channel: 137, freq: 6635 },
  { channel: 141, freq: 6655 },
  { channel: 145, freq: 6675 },
  { channel: 149, freq: 6695 },
  { channel: 153, freq: 6715 },
  { channel: 157, freq: 6735 },
  { channel: 161, freq: 6755 },
  { channel: 165, freq: 6775 },
  { channel: 169, freq: 6795 },
  { channel: 173, freq: 6815 },
  { channel: 177, freq: 6835 },
  { channel: 181, freq: 6855 },
  { channel: 185, freq: 6875 },
  { channel: 189, freq: 6895 },
  { channel: 193, freq: 6915 },
  { channel: 197, freq: 6935 },
  { channel: 201, freq: 6955 },
  { channel: 205, freq: 6975 },
  { channel: 209, freq: 6995 },
  { channel: 213, freq: 7015 },
  { channel: 217, freq: 7035 },
  { channel: 221, freq: 7055 },
  { channel: 225, freq: 7075 },
  { channel: 229, freq: 7095 },
  { channel: 233, freq: 7115 }
];

export default function TapDot11CoverageMap(props) {

  const tap = props.tap;

  if (!tap.active) {
    return <div className="alert alert-warning">No recent data.</div>
  }

  if (!tap.dot11_frequencies || tap.dot11_frequencies.length === 0) {
    return <div className="alert alert-info mb-0">This tap has no 802.11/WiFi captures collecting data.</div>
  }

  const frequencies = {};
  tap.dot11_frequencies.forEach((freq) => { frequencies[freq.frequency] = freq.channel_widths; });

  const wifiCaptures = tap.captures.filter((c) => c.capture_type === "WiFi");

  const isSupported = (freq, width) => {
    return frequencies[freq] && frequencies[freq].includes(width)
  }

  const na = () => {
    return <i className="fa-regular fa-circle channel-width-na" title="Channel width is not part of the 802.11 standard."></i>
  }

  const supported = () => {
    return <i className="fa-solid fa-circle channel-width-supported" title="Channel width is monitored by at least one WiFi capture of this tap."></i>
  }

  const unsupported = () => {
    return <i className="fa-solid fa-circle channel-width-unsupported" title="Channel width is not monitored by any WiFi captures of this tap."></i>
  }

  return (
      <React.Fragment>
        <h4>Capture Cycle Times</h4>

        <p className="text-muted">
          A WiFi capture should ideally scan its entire assigned spectrum within 60 seconds. Longer durations can
          complicate traffic analysis, as the default chart bucket size is one minute and may not encompass
          complete <a href="https://go.nzyme.org/wifi-hopping" target="_blank">channel hop cycles</a> Consider
          re-assigning channels to other captures if the current capture process is too slow.
        </p>

        <table className="table table-sm mb-3">
          <thead>
          <tr>
            <th>Capture</th>
            <th>Cycle Time</th>
            <th>Status</th>
          </tr>
          </thead>
          <tbody>
          {Object.keys(wifiCaptures.sort((a, b) => a.interface_name.localeCompare(b.interface_name))).map((key, i) => {
            return (
                <tr key={i}>
                  <td>{wifiCaptures[key].interface_name}</td>
                  <td>{numeral(wifiCaptures[key].cycle_time).format("0,0")} ms</td>
                  <td>{wifiCaptures[key].cycle_time < 60000 ? <span className="badge bg-success">Good</span> :
                      <span className="badge bg-warning">Slow</span>}</td>
                </tr>
            )
          })}
          </tbody>
        </table>

        <h4>Legend</h4>
        <ul style={{listStyleType: "none", margin: 0, padding: 0}} className="mb-3">
          <li>{supported()} Channel width is monitored by at least one WiFi capture of this tap.</li>
          <li>{unsupported()} Channel width is not monitored by any WiFi captures of this tap.</li>
          <li>{na()} Channel width is not part of the 802.11 standard.</li>
        </ul>

        <div className="sticky-head-table" style={{height: 650}}>
          <table className="table table-sm ">
            <thead>
            <tr>
              <th>Band</th>
              <th>Channel</th>
              <th>20 MHz Center Frequency</th>
              <th>20 MHz</th>
              <th>40- MHz</th>
              <th>40+ MHz</th>
              <th>80 MHz</th>
              <th>160 MHz</th>
              <th>320 MHz</th>
            </tr>
            </thead>
            <tbody>
            {channels24.map((channel, i) =>
                <tr key={i}>
                  {i === 0 ? <td rowSpan={channels24.length} className="channel-width-band">
                    <div>2.4 GHz</div>
                  </td> : null}
                  <td>{channel.channel}</td>
                  <td>{numeral(channel.freq).format("0,0")} MHz</td>
                  <td>{isSupported(channel.freq, "20") ? supported() : unsupported()}</td>
                  <td>{channel.channel >= 5 && channel.channel !== 14 ? (isSupported(channel.freq, "40MINUS") ? supported() : unsupported()) : na()}</td>
                  <td>{channel.channel <= 9 && channel.channel !== 14 ? (isSupported(channel.freq, "40PLUS") ? supported() : unsupported()) : na()}</td>
                  <td>{na()}</td>
                  <td>{na()}</td>
                  <td>{na()}</td>
                </tr>
            )}

            {channels5.map((channel, i) =>
                <tr key={i}>
                  {i === 0 ? <td rowSpan={channels5.length} className="channel-width-band">5 GHz</td> : null}
                  <td>{channel.channel}</td>
                  <td>{numeral(channel.freq).format("0,0")} MHz</td>
                  <td>{isSupported(channel.freq, "20") ? supported() : unsupported()}</td>
                  <td>{channel.channel !== 32 && channel.channel !== 36 && channel.channel !== 68 && channel.channel !== 96 && channel.channel !== 100 && channel.channel !== 149 ? (isSupported(channel.freq, "40MINUS") ? supported() : unsupported()) : na()}</td>
                  <td>{channel.channel !== 32 && channel.channel !== 64 && channel.channel !== 68 && channel.channel !== 96 && channel.channel !== 144 && channel.channel !== 165 && channel.channel !== 177 ? (isSupported(channel.freq, "40PLUS") ? supported() : unsupported()) : na()}</td>
                  <td>{channel.channel !== 32 && channel.channel !== 96 ? (isSupported(channel.freq, "80") ? supported() : unsupported()) : na()}</td>
                  <td>{channel.channel !== 32 && channel.channel !== 68 && channel.channel !== 96 && channel.channel !== 169 && channel.channel !== 173 && channel.channel !== 177 ? (isSupported(channel.freq, "160") ? supported() : unsupported()) : na()}</td>
                  <td>{na()}</td>
                </tr>
            )}

            {channels6.map((channel, i) =>
                <tr key={i}>
                  {i === 0 ? <td rowSpan={channels6.length} className="channel-width-band">6 GHz</td> : null}
                  <td>{channel.channel}</td>
                  <td>{numeral(channel.freq).format("0,0")} MHz</td>
                  <td>{isSupported(channel.freq, "20") ? supported() : unsupported()}</td>
                  <td>{channel.channel !== 1 && channel.channel !== 2 && channel.channel !== 233 ? (isSupported(channel.freq, "40MINUS") ? supported() : unsupported()) : na()}</td>
                  <td>{channel.channel !== 2 && channel.channel !== 233 ? (isSupported(channel.freq, "40PLUS") ? supported() : unsupported()) : na()}</td>
                  <td>{channel.channel !== 2 && channel.channel !== 225 && channel.channel !== 229 && channel.channel !== 233 ? (isSupported(channel.freq, "80") ? supported() : unsupported()) : na()}</td>
                  <td>{channel.channel !== 2 && channel.channel !== 225 && channel.channel !== 229 && channel.channel !== 233 ? (isSupported(channel.freq, "160") ? supported() : unsupported()) : na()}</td>
                  <td>{channel.channel !== 2 && channel.channel !== 1 && channel.channel !== 5 && channel.channel !== 9 && channel.channel !== 13 && channel.channel !== 17 && channel.channel !== 21 && channel.channel !== 25 && channel.channel !== 29 && channel.channel !== 193 && channel.channel !== 197 && channel.channel !== 201 && channel.channel !== 205 && channel.channel !== 209 && channel.channel !== 213 && channel.channel !== 217 && channel.channel !== 221 && channel.channel !== 225 && channel.channel !== 229 && channel.channel !== 233 ? (isSupported(channel.freq, "320") ? supported() : unsupported()) : na()}</td>
                </tr>
            )}
            </tbody>
          </table>
        </div>
      </React.Fragment>
  )

}
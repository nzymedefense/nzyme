import React from 'react';
import moment from 'moment';
import {formatDuration} from "./TimelineDuration";
import SignalStrength from "../SignalStrength";

function formatTimestamp(ts) {
  return moment(ts).format('YYYY-MM-DD HH:mm:ss');
}

function SSIDList({ssids}) {
  return (
    <span className="timeline-event-ssid-list">
      {ssids.map((ssid, i) => (
        <React.Fragment key={ssid}>
          <code className="timeline-event-ssid">{ssid}</code>
          {i < ssids.length - 1 && <span className="timeline-event-ssid-sep">·</span>}
        </React.Fragment>
      ))}
    </span>
  );
}

function GoneEvent({event}) {
  const {gap_start, gap_end, minutes, ongoing} = event.event_details;

  return (
    <li className="timeline-event timeline-event-gone">
      <div className="timeline-event-gone-rule" data-ongoing={ongoing ? 'true' : 'false'}>
        <span className="timeline-event-gone-label">
          {ongoing ? 'Still gone' : 'Was gone'} · {formatDuration(minutes)}
        </span>
      </div>
      <div className="timeline-event-gone-range">
        {formatTimestamp(gap_start)}
        <span className="timeline-event-gone-arrow">→</span>
        {ongoing
          ? <span className="timeline-event-gone-ongoing">still not seen</span>
          : <span>returned {formatTimestamp(gap_end)}</span>}
      </div>
    </li>
  );
}

function ActiveNowEvent({event}) {
  return (
    <li className="timeline-event timeline-event-active-now">
      <div className="timeline-event-marker" data-kind="active" />
      <div className="timeline-event-body">
        <div className="timeline-event-header">
          <span className="timeline-event-type">Currently active</span>
        </div>
        <div className="timeline-event-detail">
          <span className="timeline-event-active-detail">
            Last seen {moment(event.event_details.since).fromNow()}
          </span>
        </div>
      </div>
    </li>
  );
}

function StrongestTapEvent({event}) {
  const {strongest_tap_name, strongest_tap_rssi, previous_tap_name, previous_tap_rssi} = event.event_details;
  const hasPrevious = previous_tap_name && previous_tap_rssi;

  return (
    <li className="timeline-event">
      <div className="timeline-event-marker" data-kind="tap" />
      <div className="timeline-event-body">
        <div className="timeline-event-header">
          <span className="timeline-event-type">Strongest tap</span>
          <span className="timeline-event-sep">·</span>
          <span className="timeline-event-time">{formatTimestamp(event.timestamp)}</span>
        </div>
        <div className="timeline-event-tap-row">
          <span className="timeline-event-tap-row-prefix">now</span>
          <span className="timeline-event-tap-name">{strongest_tap_name}</span>
          <SignalStrength strength={strongest_tap_rssi} />
        </div>
        {hasPrevious ? (
          <div className="timeline-event-tap-row timeline-event-tap-row-prev">
            <span className="timeline-event-tap-row-prefix">was</span>
            <span className="timeline-event-tap-name">{previous_tap_name}</span>
            <SignalStrength strength={previous_tap_rssi} />
          </div>
        ) : (
          <div className="timeline-event-tap-row timeline-event-tap-row-prev">
            <span className="timeline-event-tap-row-prefix">was</span>
            <span className="timeline-event-tap-row-empty">first observation</span>
          </div>
        )}
      </div>
    </li>
  );
}

function SSIDDiffEvent({event}) {
  const {new_ssids, disappeared_ssids, known_ssids} = event.event_details;
  const hasNew = new_ssids && new_ssids.length > 0;
  const hasGone = disappeared_ssids && disappeared_ssids.length > 0;
  const hasKnown = known_ssids && known_ssids.length > 0;

  return (
    <li className="timeline-event">
      <div className="timeline-event-marker" data-kind="ssid" />
      <div className="timeline-event-body">
        <div className="timeline-event-header">
          <span className="timeline-event-type">SSID change</span>
          <span className="timeline-event-sep">·</span>
          <span className="timeline-event-time">{formatTimestamp(event.timestamp)}</span>
        </div>
        {hasNew && (
          <div className="timeline-event-ssid-row">
            <span className="timeline-event-ssid-row-prefix">added</span>
            <SSIDList ssids={new_ssids} />
          </div>
        )}
        {hasGone && (
          <div className="timeline-event-ssid-row">
            <span className="timeline-event-ssid-row-prefix">removed</span>
            <SSIDList ssids={disappeared_ssids} />
          </div>
        )}
        {hasKnown && (
          <div className="timeline-event-ssid-row timeline-event-ssid-row-known">
            <span className="timeline-event-ssid-row-prefix">all</span>
            <SSIDList ssids={known_ssids} />
          </div>
        )}
      </div>
    </li>
  );
}

function FingerprintDiffEvent({event}) {
  const {new_fingerprints, disappeared_fingerprints, known_fingerprints} = event.event_details;
  const hasNew = new_fingerprints && new_fingerprints.length > 0;
  const hasGone = disappeared_fingerprints && disappeared_fingerprints.length > 0;
  const hasKnown = known_fingerprints && known_fingerprints.length > 0;

  return (
    <li className="timeline-event">
      <div className="timeline-event-marker" data-kind="fingerprint" />
      <div className="timeline-event-body">
        <div className="timeline-event-header">
          <span className="timeline-event-type">Fingerprint change</span>
          <span className="timeline-event-sep">·</span>
          <span className="timeline-event-time">{formatTimestamp(event.timestamp)}</span>
        </div>
        {hasNew && (
          <div className="timeline-event-ssid-row">
            <span className="timeline-event-ssid-row-prefix">added</span>
            <SSIDList ssids={new_fingerprints} />
          </div>
        )}
        {hasGone && (
          <div className="timeline-event-ssid-row">
            <span className="timeline-event-ssid-row-prefix">removed</span>
            <SSIDList ssids={disappeared_fingerprints} />
          </div>
        )}
        {hasKnown && (
          <div className="timeline-event-ssid-row timeline-event-ssid-row-known">
            <span className="timeline-event-ssid-row-prefix">all</span>
            <SSIDList ssids={known_fingerprints} />
          </div>
        )}
      </div>
    </li>
  );
}

export default function TimelineEvent({event}) {
  switch (event.event_type) {
    case 'GONE':
      return <GoneEvent event={event} />;
    case 'DOT11_BSSID_STRONGEST_TAP':
      return <StrongestTapEvent event={event} />;
    case 'DOT11_BSSID_SSID_DIFF':
      return <SSIDDiffEvent event={event} />;
    case 'DOT11_BSSID_FINGERPRINT_DIFF':
      return <FingerprintDiffEvent event={event} />;
    case 'SYNTHETIC_ACTIVE':
      return <ActiveNowEvent event={event} />;
    default:
      return (
        <li className="timeline-event">
          <div className="timeline-event-marker" data-kind="unknown" />
          <div className="timeline-event-body">
            <div className="timeline-event-header">
              <span className="timeline-event-type">{event.event_type}</span>
              <span className="timeline-event-sep">·</span>
              <span className="timeline-event-time">{formatTimestamp(event.timestamp)}</span>
            </div>
          </div>
        </li>
      );
  }
}
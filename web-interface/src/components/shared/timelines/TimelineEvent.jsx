import React from 'react';
import moment from 'moment';
import {formatDuration} from "./TimelineDuration";
import SignalStrength from "../SignalStrength";

function formatTimestamp(ts) {
  return <span title={moment(ts).fromNow()}>{moment(ts).format('YYYY-MM-DD HH:mm:ss Z')}</span>
}

function AttributeList({attributes}) {
  return (
    <span className="timeline-event-attribute-list">
      {attributes.map((attribute, i) => (
        <React.Fragment key={attribute}>
          <code className="timeline-event-attribute">{attribute}</code>
          {i < attributes.length - 1 && <span className="timeline-event-attribute-sep">·</span>}
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
          <div className="timeline-event-attribute-row">
            <span className="timeline-event-attribute-row-prefix">added</span>
            <AttributeList attributes={new_ssids} />
          </div>
        )}
        {hasGone && (
          <div className="timeline-event-attribute-row">
            <span className="timeline-event-attribute-row-prefix">removed</span>
            <AttributeList attributes={disappeared_ssids} />
          </div>
        )}
        {hasKnown && (
          <div className="timeline-event-attribute-row timeline-event-attribute-row-known">
            <span className="timeline-event-attribute-row-prefix">all</span>
            <AttributeList attributes={known_ssids} />
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
          <div className="timeline-event-attribute-row">
            <span className="timeline-event-attribute-row-prefix">added</span>
            <AttributeList attributes={new_fingerprints} />
          </div>
        )}
        {hasGone && (
          <div className="timeline-event-attribute-row">
            <span className="timeline-event-attribute-row-prefix">removed</span>
            <AttributeList attributes={disappeared_fingerprints} />
          </div>
        )}
        {hasKnown && (
          <div className="timeline-event-attribute-row timeline-event-attribute-row-known">
            <span className="timeline-event-attribute-row-prefix">all</span>
            <AttributeList attributes={known_fingerprints} />
          </div>
        )}
      </div>
    </li>
  );
}

function RatesDiffEvent({event}) {
  const {new_rates, disappeared_rates, known_rates} = event.event_details;
  const byValue = (a, b) => a - b;
  const added = new_rates ? [...new_rates].sort(byValue) : [];
  const removed = disappeared_rates ? [...disappeared_rates].sort(byValue) : [];
  const all = known_rates ? [...known_rates].sort(byValue) : [];
  const hasNew = added.length > 0;
  const hasGone = removed.length > 0;
  const hasKnown = all.length > 0;
  const firstObservation = hasKnown && hasNew && !hasGone && added.length === all.length;

  return (
    <li className="timeline-event">
      <div className="timeline-event-marker" data-kind="rates" />
      <div className="timeline-event-body">
        <div className="timeline-event-header">
          <span className="timeline-event-type">Rates change</span>
          <span className="timeline-event-type-unit">Mbps</span>
          <span className="timeline-event-sep">·</span>
          <span className="timeline-event-time">{formatTimestamp(event.timestamp)}</span>
        </div>
        {firstObservation ? (
          <div className="timeline-event-attribute-row timeline-event-attribute-row-known">
            <span className="timeline-event-attribute-row-prefix">first observation</span>
            <AttributeList attributes={all} />
          </div>
        ) : (
          <>
            {hasNew && (
              <div className="timeline-event-attribute-row">
                <span className="timeline-event-attribute-row-prefix">added</span>
                <AttributeList attributes={added} />
              </div>
            )}
            {hasGone && (
              <div className="timeline-event-attribute-row">
                <span className="timeline-event-attribute-row-prefix">removed</span>
                <AttributeList attributes={removed} />
              </div>
            )}
            {hasKnown && (
              <div className="timeline-event-attribute-row timeline-event-attribute-row-known">
                <span className="timeline-event-attribute-row-prefix">all</span>
                <AttributeList attributes={all} />
              </div>
            )}
          </>
        )}
      </div>
    </li>
  );
}

function SecurityProtocolsDiffEvent({event}) {
  const {new_protocols, disappeared_protocols, known_protocols} = event.event_details;
  const hasNew = new_protocols && new_protocols.length > 0;
  const hasGone = disappeared_protocols && disappeared_protocols.length > 0;
  const hasKnown = known_protocols && known_protocols.length > 0;
  const firstObservation = hasKnown && hasNew && !hasGone && new_protocols.length === known_protocols.length;

  return (
    <li className="timeline-event">
      <div className="timeline-event-marker" data-kind="protocols" />
      <div className="timeline-event-body">
        <div className="timeline-event-header">
          <span className="timeline-event-type">Security protocols change</span>
          <span className="timeline-event-sep">·</span>
          <span className="timeline-event-time">{formatTimestamp(event.timestamp)}</span>
        </div>
        {firstObservation ? (
          <div className="timeline-event-attribute-row timeline-event-attribute-row-known">
            <span className="timeline-event-attribute-row-prefix">first observation</span>
            <AttributeList attributes={known_protocols} />
          </div>
        ) : (
          <>
            {hasNew && (
              <div className="timeline-event-attribute-row">
                <span className="timeline-event-attribute-row-prefix">added</span>
                <AttributeList attributes={new_protocols} />
              </div>
            )}
            {hasGone && (
              <div className="timeline-event-attribute-row">
                <span className="timeline-event-attribute-row-prefix">removed</span>
                <AttributeList attributes={disappeared_protocols} />
              </div>
            )}
            {hasKnown && (
              <div className="timeline-event-attribute-row timeline-event-attribute-row-known">
                <span className="timeline-event-attribute-row-prefix">all</span>
                <AttributeList attributes={known_protocols} />
              </div>
            )}
          </>
        )}
      </div>
    </li>
  );
}

function SecuritySuitesDiffEvent({event}) {
  const {new_suites, disappeared_suites, known_suites} = event.event_details;
  const hasNew = new_suites && new_suites.length > 0;
  const hasGone = disappeared_suites && disappeared_suites.length > 0;
  const hasKnown = known_suites && known_suites.length > 0;
  const firstObservation = hasKnown && hasNew && !hasGone && new_suites.length === known_suites.length;

  return (
    <li className="timeline-event">
      <div className="timeline-event-marker" data-kind="suites" />
      <div className="timeline-event-body">
        <div className="timeline-event-header">
          <span className="timeline-event-type">Security suites change</span>
          <span className="timeline-event-sep">·</span>
          <span className="timeline-event-time">{formatTimestamp(event.timestamp)}</span>
        </div>
        {firstObservation ? (
          <div className="timeline-event-attribute-row timeline-event-attribute-row-known">
            <span className="timeline-event-attribute-row-prefix">first observation</span>
            <AttributeList attributes={known_suites} />
          </div>
        ) : (
          <>
            {hasNew && (
              <div className="timeline-event-attribute-row">
                <span className="timeline-event-attribute-row-prefix">added</span>
                <AttributeList attributes={new_suites} />
              </div>
            )}
            {hasGone && (
              <div className="timeline-event-attribute-row">
                <span className="timeline-event-attribute-row-prefix">removed</span>
                <AttributeList attributes={disappeared_suites} />
              </div>
            )}
            {hasKnown && (
              <div className="timeline-event-attribute-row timeline-event-attribute-row-known">
                <span className="timeline-event-attribute-row-prefix">all</span>
                <AttributeList attributes={known_suites} />
              </div>
            )}
          </>
        )}
      </div>
    </li>
  );
}

function ActiveChannelEvent({event}) {
  const {active_channel, active_channel_freq, previous_channel, previous_channel_freq} = event.event_details;
  const hasPrevious = previous_channel != null;

  return (
    <li className="timeline-event">
      <div className="timeline-event-marker" data-kind="channel" />
      <div className="timeline-event-body">
        <div className="timeline-event-header">
          <span className="timeline-event-type">Active channel</span>
          <span className="timeline-event-sep">·</span>
          <span className="timeline-event-time">{formatTimestamp(event.timestamp)}</span>
        </div>
        <div className="timeline-event-channel-row">
          <span className="timeline-event-channel-row-prefix">now</span>
          <span className="timeline-event-channel-name">channel {active_channel}</span>
          {active_channel_freq && (
            <span className="timeline-event-channel-freq">{active_channel_freq} MHz</span>
          )}
        </div>
        {hasPrevious ? (
          <div className="timeline-event-channel-row timeline-event-channel-row-prev">
            <span className="timeline-event-channel-row-prefix">was</span>
            <span className="timeline-event-channel-name">channel {previous_channel}</span>
            {previous_channel_freq && (
              <span className="timeline-event-channel-freq">{previous_channel_freq} MHz</span>
            )}
          </div>
        ) : (
          <div className="timeline-event-channel-row timeline-event-channel-row-prev">
            <span className="timeline-event-channel-row-prefix">was</span>
            <span className="timeline-event-channel-row-empty">first observation</span>
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
    case 'DOT11_SSID_RATES_DIFF':
      return <RatesDiffEvent event={event} />;
    case 'DOT11_SSID_SECURITY_PROTOCOLS_DIFF':
      return <SecurityProtocolsDiffEvent event={event} />;
    case 'DOT11_SSID_SECURITY_SUITES_DIFF':
      return <SecuritySuitesDiffEvent event={event} />;
    case 'DOT11_SSID_FINGERPRINTS_DIFF':
      return <FingerprintDiffEvent event={event} />;
    case 'DOT11_SSID_ACTIVE_CHANNEL':
    case 'DOT11_BSSID_ACTIVE_CHANNEL':
      return <ActiveChannelEvent event={event} />;
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
import React from 'react';

const ACTION_LABELS = {
  monitor: "Monitor",
  investigate: "Investigate",
  escalate: "Escalate",
  block: "Block",
  none: "None",
};

export default function LLMResponse({data, type}) {

  if (type !== "DOT11_BSSID_TIMELINE_SUMMARY") {
    return <div className="alert alert-danger">Unknown response type.</div>
  }

  if (data === null) {
    return null;
  }

  const classification = data.threat_classification || "Unknown";
  const confidence = data.confidence || "Low";
  const action = (data.recommended_action || "none").toLowerCase();
  const actionLabel = ACTION_LABELS[action] || "None";

  const hasIndicators = data.threat_indicators &&
    data.threat_indicators.length > 0 &&
    !(data.threat_indicators.length === 1 && data.threat_indicators[0].toLowerCase() === "none");

  const deviceTypeClean = (data.device_type || "")
    .replace(/\s*\.\s*Confidence:\s*(High|Medium|Low)\.?\s*$/i, "")
    .trim();

  return (
    <div className="llm-summary mt-3 mb-3">
      <div className="llm-summary-verdict">
        <span className="llm-summary-verdict-item">
          <span className="llm-summary-verdict-label">Classification</span>
          <span className={`llm-summary-verdict-value llm-verdict-${classification.toLowerCase()}`}>
            {classification}
          </span>
        </span>
        <span className="llm-summary-verdict-item">
          <span className="llm-summary-verdict-label">Confidence</span>
          <span className="llm-summary-verdict-value">{confidence}</span>
        </span>
        <span className="llm-summary-verdict-item">
          <span className="llm-summary-verdict-label">Action</span>
          <span className="llm-summary-verdict-value">{actionLabel}</span>
        </span>
      </div>

      <dl className="llm-fields">
        <dt>Device Type</dt>
        <dd>{deviceTypeClean}</dd>

        <dt>Mobility</dt>
        <dd>{data.mobility}</dd>

        <dt>Advertised SSIDs</dt>
        <dd>{data.advertised_ssids}</dd>

        <dt>Schedule</dt>
        <dd>{data.schedule}</dd>

        <dt>Lifecycle</dt>
        <dd>{data.lifecycle}</dd>

        {hasIndicators && (
          <>
            <dt>Threat Indicators</dt>
            <dd>
              <ul className="llm-indicators">
                {data.threat_indicators.map((ind, i) => (
                  <li key={i}>{ind}</li>
                ))}
              </ul>
            </dd>
          </>
        )}
      </dl>
    </div>
  );
}
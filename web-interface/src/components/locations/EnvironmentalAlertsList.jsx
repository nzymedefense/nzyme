import React, {useState} from "react";

export default function EnvironmentalAlertsList({environment}) {

  const [selectedAlert, setSelectedAlert] = useState(null);

  if (!environment || !environment.alerts || environment.alerts.length === 0) {
    return (
      <div className="h-100 d-flex flex-column justify-content-center align-items-center text-muted">
        <i className="fa-solid fa-circle-check fa-2x text-success mb-2"></i>
        <div>No active environmental alerts.</div>
      </div>
    );
  }

  const severityBadge = (severity) => {
    let cls;
    switch (severity) {
      case "Extreme":
      case "Severe":
        cls = "bg-danger";
        break;
      case "Moderate":
        cls = "bg-warning text-dark";
        break;
      case "Minor":
        cls = "bg-info text-dark";
        break;
      default:
        cls = "bg-secondary";
    }
    return <span className={`badge ${cls}`} style={{position: "relative", top: 7}}>{severity || "Unknown"}</span>;
  };

  const severityBorderColor = (severity) => {
    switch (severity) {
      case "Extreme":
      case "Severe": return "var(--bs-danger)";
      case "Moderate": return "var(--bs-warning)";
      case "Minor": return "var(--bs-info)";
      default: return "var(--bs-secondary)";
    }
  };

  const formatTime = (iso) => {
    if (!iso) return null;
    return new Date(iso).toLocaleString(undefined, {
      dateStyle: "medium",
      timeStyle: "short"
    });
  };

  return (
    <>
      <div className="list-group">
        {environment.alerts.map((alert, i) => (
          <button key={i}
                  type="button"
                  className="list-group-item list-group-item-action"
                  style={{borderLeft: `4px solid ${severityBorderColor(alert.severity)}`}}
                  onClick={() => setSelectedAlert(alert)}>
            <div className="d-flex justify-content-between align-items-start mb-1">
              <strong>{alert.event || "Alert"}</strong>
              {severityBadge(alert.severity)}
            </div>
            {alert.headline && (
              <div className="mb-1">{alert.headline}</div>
            )}
            <div className="text-muted small">
              {alert.expires && <>Expires {formatTime(alert.expires)}</>}
              {alert.urgency && alert.urgency !== "Unknown" && <> · {alert.urgency}</>}
            </div>
          </button>
        ))}
      </div>

      {selectedAlert && (
        <div className="modal show d-block" tabIndex="-1"
             style={{backgroundColor: "rgba(0,0,0,0.5)"}}
             onClick={(e) => { if (e.target === e.currentTarget) setSelectedAlert(null); }}>
          <div className="modal-dialog modal-lg modal-dialog-scrollable">
            <div className="modal-content">
              <div className="modal-header">
                <h5 className="modal-title d-flex align-items-center gap-2">
                  {selectedAlert.event || "Alert"}
                  {severityBadge(selectedAlert.severity)}
                </h5>
                <button type="button" className="btn-close"
                        onClick={() => setSelectedAlert(null)}></button>
              </div>
              <div className="modal-body">
                {selectedAlert.headline && (
                  <h6 className="mb-3">{selectedAlert.headline}</h6>
                )}
                <div className="text-muted mb-3">
                  {selectedAlert.effective && <>From {formatTime(selectedAlert.effective)}</>}
                  {selectedAlert.expires && <> · Expires {formatTime(selectedAlert.expires)}</>}
                  {selectedAlert.ends && <> · Ends {formatTime(selectedAlert.ends)}</>}
                  {selectedAlert.certainty && <> · Certainty: {selectedAlert.certainty}</>}
                  {selectedAlert.urgency && <> · Urgency: {selectedAlert.urgency}</>}
                </div>
                {selectedAlert.description && (
                  <div style={{whiteSpace: "pre-wrap"}} className="small">
                    {selectedAlert.description}
                  </div>
                )}
                {selectedAlert.sender_name && (
                  <div className="text-muted small mt-3">— {selectedAlert.sender_name}</div>
                )}
              </div>
              <div className="modal-footer">
                <button type="button" className="btn btn-secondary btn-sm"
                        onClick={() => setSelectedAlert(null)}>Close</button>
              </div>
            </div>
          </div>
        </div>
      )}
    </>
  );

}
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
    return <span className={`badge px-2 py-1 ${cls}`}>{severity || "Unknown"}</span>;
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

  const renderDescription = (desc) => {
    if (!desc) return null;
    return desc.split(/\n\n+/).map((para, i) => {
      const cleaned = para.replace(/\n/g, ' ').trim();
      if (!cleaned) return null;
      const parts = cleaned.split(/(https?:\/\/\S+)/g);
      return (
        <p key={i} className="mb-2">
          {parts.map((part, j) =>
            /^https?:\/\//.test(part)
              ? <a key={j} href={part} target="_blank" rel="noopener noreferrer">{part}</a>
              : part
          )}
        </p>
      );
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
              <span style={{position: "relative", top: 1, left: 5}}>{severityBadge(alert.severity)}</span>
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
            <div className="modal-content" style={{borderTop: `4px solid ${severityBorderColor(selectedAlert.severity)}`}}>
              <div className="modal-header">
                <h2 className="modal-title d-flex align-items-center gap-2">
                  {selectedAlert.event || "Alert"}
                  <span style={{position: "relative", top: 1, left: 5}}>{severityBadge(selectedAlert.severity)}</span>
                </h2>
                <button type="button" className="btn-close"
                        onClick={() => setSelectedAlert(null)}></button>
              </div>
              <div className="modal-body">
                {selectedAlert.headline && (
                  <h3 className="mb-3" style={{fontVariant: "all-small-caps"}}>
                    {selectedAlert.headline}
                  </h3>
                )}

                <dl className="row mb-3">
                  {selectedAlert.effective && <>
                    <dt className="col-sm-3 fw-normal text-muted">Effective</dt>
                    <dd className="col-sm-9 mb-1">{formatTime(selectedAlert.effective)}</dd>
                  </>}
                  {selectedAlert.expires && <>
                    <dt className="col-sm-3 fw-normal text-muted">Expires</dt>
                    <dd className="col-sm-9 mb-1">{formatTime(selectedAlert.expires)}</dd>
                  </>}
                  {selectedAlert.ends && <>
                    <dt className="col-sm-3 fw-normal text-muted">Ends</dt>
                    <dd className="col-sm-9 mb-1">{formatTime(selectedAlert.ends)}</dd>
                  </>}
                  {selectedAlert.certainty && <>
                    <dt className="col-sm-3 fw-normal text-muted">Certainty</dt>
                    <dd className="col-sm-9 mb-1">{selectedAlert.certainty}</dd>
                  </>}
                  {selectedAlert.urgency && <>
                    <dt className="col-sm-3 fw-normal text-muted">Urgency</dt>
                    <dd className="col-sm-9 mb-0">{selectedAlert.urgency}</dd>
                  </>}
                </dl>

                {selectedAlert.description && (
                  <div className="border-top pt-3">
                    {renderDescription(selectedAlert.description)}
                  </div>
                )}

                {selectedAlert.sender_name && (
                  <div className="text-muted small mt-3 fst-italic text-end">
                    Issued by {selectedAlert.sender_name}
                  </div>
                )}
              </div>
              <div className="modal-footer">
                <button type="button" className="btn btn-secondary" onClick={() => setSelectedAlert(null)}>
                  Close
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </>
  );

}
import React, {useState} from "react";
import Dot11Service from "../../../services/Dot11Service";
import {notify} from "react-notify-toast";

const dot11Service = new Dot11Service();

function ToggleMonitoringStatusButton(props) {

  const bumpRevision = props.bumpRevision;
  const ssid = props.ssid;

  const [submitting, setSubmitting] = useState(false);

  const enable = () => {
    setSubmitting(true);
    dot11Service.enableMonitoredNetwork(ssid.uuid, function() {
      bumpRevision();
      notify.show("Monitoring enabled.", "success");
      setSubmitting(false);
    }, function () {
      notify.show("Could not enable monitoring. Please check nzyme log file.", "error");
      setSubmitting(false);
    });
  }

  const disable = () => {
    setSubmitting(true);
    dot11Service.disableMonitoredNetwork(ssid.uuid, function() {
      bumpRevision();
      notify.show("Monitoring disabled.", "success");
      setSubmitting(false);
    }, function () {
      notify.show("Could not disable monitoring. Please check nzyme log file.", "error");
      setSubmitting(false);
    });
  }

  if (ssid.is_enabled) {
    return <button className="btn btn-outline-secondary" onClick={disable}>
      {submitting ? "Please wait ..." : "Disable"}
    </button>
  }  else {
    return <button className="btn btn-success" onClick={enable}>
      {submitting ? "Please wait ..." : "Enable"}
    </button>
  }

}

export default ToggleMonitoringStatusButton;
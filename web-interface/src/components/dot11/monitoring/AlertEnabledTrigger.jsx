import React, {useEffect, useState} from "react";
import Dot11Service from "../../../services/Dot11Service";
import {notify} from "react-notify-toast";

const dot11Service = new Dot11Service();

function AlertEnabledTrigger(props) {

  const ssid = props.ssid;
  const parameter = props.parameter;
  const bumpRevision = props.bumpRevision;

  const [status, setStatus] = useState(null);

  useEffect(() => {
    setStatus(ssid["enabled_" + parameter]);
  }, [ssid]);

  const enable = (e) => {
    e.preventDefault();

    if (!confirm("Really enable this alert?")) {
      return;
    }

    dot11Service.setMonitoredNetworkAlertEnabledStatus(ssid.uuid, parameter, true, () => {
      notify.show("Alert enabled.", "success");
      setStatus(true);
      bumpRevision();
    })
  }

  const disable = (e) => {
    e.preventDefault();

    if (!confirm("Really disable this alert?")) {
      return;
    }

    dot11Service.setMonitoredNetworkAlertEnabledStatus(ssid.uuid, parameter, false, () => {
      notify.show("Alert disabled.", "success");
      setStatus(false);
      bumpRevision();
    })
  }

  if (status) {
    return (
        <a href="#" title="Disable this alert." onClick={disable}>Disable</a>
    )
  } else {
    return (
        <a href="#" title="Enable this alert." onClick={enable}>Enable</a>
    )
  }

}

export default AlertEnabledTrigger;
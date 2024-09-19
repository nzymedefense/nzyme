import React from 'react';
import LoadingSpinner from "../../../misc/LoadingSpinner";
import Dot11Service from "../../../../services/Dot11Service";

const dot11Service = new Dot11Service();

export default function SSIDMonitoringConfiguration(props) {

  const configuration = props.configuration;
  const organizationUUID = props.organizationUUID;
  const tenantUUID = props.tenantUUID;
  const onChange = props.onChange;

  const onDisable = () => {
    if (!confirm("Really disable SSID monitoring?")) {
      return;
    }

    dot11Service.disableSSIDMonitoring(organizationUUID, tenantUUID, onChange)
  }

  const onEnable = () => {
    if (!confirm("Really enable SSID monitoring?")) {
      return;
    }

    dot11Service.enableSSIDMonitoring(organizationUUID, tenantUUID, onChange)
  }

  if (configuration === null) {
    return <LoadingSpinner />
  }

  if (configuration.is_enabled) {
    return (
        <div>
          <span className="text-success text-bold">
            <i className="fa fa-check" /> SSID Monitoring is enabled.
          </span>

          <div className="mt-2">
            <button className="btn btn-sm btn-danger" onClick={onDisable}>
              Disable SSID Monitoring
            </button>
          </div>
        </div>
    )
  } else {
    return (
        <div>
          <span className="text-success text-warning">
            <i className="fa fa-warning" /> SSID Monitoring is disabled.
          </span>

          <div className="mt-2">
            <button className="btn btn-sm btn-success" onClick={onEnable}>
              Enable SSID Monitoring
            </button>
          </div>
        </div>
    )
  }

}
import React, {useContext, useEffect, useState} from "react";
import RelatedMonitoredNetworkData from "./RelatedMonitoredNetworkData";
import Dot11Service from "../../services/Dot11Service";
import MonitoredNetworkAlertStatusTable from "../dot11/monitoring/MonitoredNetworkAlertStatusTable";
import {UserContext} from "../../App";
import {userHasPermission} from "../../util/Tools";
import {notify} from "react-notify-toast";

const dot11Service = new Dot11Service();

function RelatedMonitoredNetwork(props) {

  const networkId = props.networkId;
  const alert = props.alert;

  const user = useContext(UserContext);

  const [ssid, setSSID] = useState(null);
  const [ssidDeleted, setSSIDDeleted] = useState(false); // In case the monitored network doesn't exist.

  useEffect(() => {
    if (userHasPermission(user, "dot11_monitoring_manage") && networkId) {
      dot11Service.findMonitoredSSID(networkId, setSSID, function() {
        /* noop */
      }, function(error) {
        if (error.response.status === 404) {
          setSSIDDeleted(true);
        } else {
          notify.show('REST call failed. (HTTP ' + error.response.status + ')', 'error')
        }
      });
    }
  }, [ssid]);

  if (!userHasPermission(user, "dot11_monitoring_manage")) {
    return (
        <div className="row mt-3">
          <div className="col-md-6">
            <div className="card">
              <div className="card-body">
                <h3>Related Network Monitor Data</h3>

                <div className="alert alert-info mb-0">
                  Your user cannot access network monitor configuration.
                </div>
              </div>
            </div>
          </div>
        </div>
    )
  }

  if (ssidDeleted) {
    return (
        <div className="row mt-3">
          <div className="col-md-6">
            <div className="card">
              <div className="card-body">
                <h3>Related Network Monitor Data</h3>

                <div className="alert alert-info mb-0">
                  Monitored network has been deleted since alert was raised.
                </div>
              </div>
            </div>
          </div>
        </div>
    )
  }

  if (!networkId) {
    return null;
  }

  return (
      <div className="row mt-3">
        <div className="col-md-6">
          <div className="card">
            <div className="card-body">
              <h3>Related Network Monitor Data</h3>

              <RelatedMonitoredNetworkData ssid={ssid} />

              {alert.is_active ? <MonitoredNetworkAlertStatusTable ssid={ssid} renderControls={false} />
                  : "Real-time network monitoring information is only displayed for currently active alerts." }
            </div>
          </div>
        </div>
      </div>
  )

}

export default RelatedMonitoredNetwork;
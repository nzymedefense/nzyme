import React, {useContext, useEffect, useState} from "react";
import LoadingSpinner from "../misc/LoadingSpinner";
import Dot11Service from "../../services/Dot11Service";
import MonitoredNetworkAlertStatusTable from "./monitoring/MonitoredNetworkAlertStatusTable";
import ApiRoutes from "../../util/ApiRoutes";
import CardTitleWithControls from "../shared/CardTitleWithControls";
import {UserContext} from "../../App";
import {userHasPermission} from "../../util/Tools";

const dot11Service = new Dot11Service();

export default function Dot11OverviewMonitoredNetworkSummary(props) {

  const uuid = props.uuid;

  const user = useContext(UserContext);

  const [network, setNetwork] = useState(null);

  useEffect(() => {
    if (userHasPermission(user, "dot11_monitoring_manage")) {
      dot11Service.findMonitoredSSID(uuid, setNetwork, () => {
      })
    }
  }, [uuid]);

  if (!userHasPermission(user, "dot11_monitoring_manage")) {
    return null;
  }

  if (!network) {
    return <LoadingSpinner />
  }

  return (
    <div className="row mt-3">
      <div className="col-12">
        <div className="card">
          <div className="card-body">
            <CardTitleWithControls title={"Monitored Network: " + network.ssid}
                                   slim={true}
                                   internalLink={ApiRoutes.DOT11.MONITORING.SSID_DETAILS(uuid)} />

            <MonitoredNetworkAlertStatusTable ssid={network} renderControls={false} />
          </div>
        </div>
      </div>
    </div>
  )

}
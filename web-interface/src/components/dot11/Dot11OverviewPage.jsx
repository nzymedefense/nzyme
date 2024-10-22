import React, {useContext, useEffect, useState} from "react";
import DetectionAlertsService from "../../services/DetectionAlertsService";
import LoadingSpinner from "../misc/LoadingSpinner";
import NumberCard from "../widgets/presentation/NumberCard";
import {userHasPermission} from "../../util/Tools";
import {TapContext, UserContext} from "../../App";
import WithPermission from "../misc/WithPermission";
import ApiRoutes from "../../util/ApiRoutes";
import AlertsTable from "../alerts/AlertsTable";
import {Presets} from "../shared/timerange/TimeRange";
import BSSIDAndSSIDChart from "./bssids/BSSIDAndSSIDChart";
import DiscoHistogram from "./disco/DiscoHistogram";
import Dot11Service from "../../services/Dot11Service";
import MonitoredNetworkAlertStatusTable from "./monitoring/MonitoredNetworkAlertStatusTable";
import Dot11OverviewMonitoredNetworkSummary from "./Dot11OverviewMonitoredNetworkSummary";
import CardTitleWithControls from "../shared/CardTitleWithControls";
import {disableTapSelector, enableTapSelector} from "../misc/TapSelector";

const alertsService = new DetectionAlertsService();
const dot11Service = new Dot11Service();

export default function Dot11OverviewPage() {

  const tapContext = useContext(TapContext);
  const user = useContext(UserContext);

  const [alerts, setAlerts] = useState(null);
  const [monitoredNetworks, setMonitoredNetworks] = useState(null);

  useEffect(() => {
    enableTapSelector(tapContext);

    return () => {
      disableTapSelector(tapContext);
    }
  }, [tapContext]);

  useEffect(() => {
    if (userHasPermission(user, "alerts_view")) {
      alertsService.findAllAlerts(setAlerts, 10, 0, "DOT11");
    }

    if (userHasPermission(user, "dot11_monitoring_manage")) {
      dot11Service.findAllMonitoredSSIDs(setMonitoredNetworks);
    }
  }, []);

  if ((userHasPermission(user, "alerts_view") && !alerts) || (userHasPermission(user, "dot11_monitoring_manage") && !monitoredNetworks)) {
    return <LoadingSpinner />
  }

  const topRowColWidth = () => {
    return userHasPermission(user, "alerts_view") ? 3 : 4;
  }

  return (
    <React.Fragment>
      <div className="row">
        <div className="col-12">
          <h1>WiFi Overview</h1>
        </div>
      </div>

      <div className="row mt-3">

        <WithPermission permission="alerts_view">
          <div className={"col-" + topRowColWidth()}>
            <NumberCard title="Active Alerts"
                        internalLink={ApiRoutes.ALERTS.INDEX}
                        value={alerts ? alerts.total_active : 0}
                        numberFormat="0,0"
                        fullHeight={true}
                        className={(alerts ? (alerts.total_active > 0 ? "bg-danger" : null) : null)}/>
          </div>
        </WithPermission>

        <div className={"col-" + topRowColWidth()}>
          <div className="card">
            <div className="card-body">
              <CardTitleWithControls title="Access Points"
                                     slim={true}
                                     internalLink={ApiRoutes.DOT11.NETWORKS.BSSIDS} />

              <BSSIDAndSSIDChart parameter="bssid_count" timeRange={Presets.RELATIVE_HOURS_24}/>
            </div>
          </div>
        </div>

        <div className={"col-" + topRowColWidth()}>
          <div className="card">
            <div className="card-body">
              <CardTitleWithControls title="Networks"
                                     slim={true}
                                     internalLink={ApiRoutes.DOT11.NETWORKS.BSSIDS} />

              <BSSIDAndSSIDChart parameter="ssid_count" timeRange={Presets.RELATIVE_HOURS_24}/>
            </div>
          </div>
        </div>

        <div className={"col-" + topRowColWidth()}>
          <div className="card">
            <div className="card-body">
              <CardTitleWithControls title="Disconnections"
                                     slim={true}
                                     internalLink={ApiRoutes.DOT11.DISCO.INDEX} />

              <DiscoHistogram discoType="disconnection" timeRange={Presets.RELATIVE_HOURS_24}/>
            </div>
          </div>
        </div>
      </div>

      <WithPermission permission="alerts_view">
        <div className="row mt-3">
          <div className="col-12">
            <div className="card">
              <div className="card-body">
                <CardTitleWithControls title="All Alerts"
                                       slim={true}
                                       internalLink={ApiRoutes.ALERTS.INDEX} />

                <AlertsTable perPage={5} hideControls={true}/>
              </div>
            </div>
          </div>
        </div>
      </WithPermission>

      {monitoredNetworks && monitoredNetworks.map((ssid, i) => {
        return <Dot11OverviewMonitoredNetworkSummary uuid={ssid.uuid} />
      })}

    </React.Fragment>
)

}
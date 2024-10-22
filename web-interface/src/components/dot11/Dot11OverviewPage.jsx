import React, {useContext, useEffect, useState} from "react";
import DetectionAlertsService from "../../services/DetectionAlertsService";
import LoadingSpinner from "../misc/LoadingSpinner";
import NumberCard from "../widgets/presentation/NumberCard";
import {userHasPermission} from "../../util/Tools";
import {UserContext} from "../../App";
import WithPermission from "../misc/WithPermission";
import ApiRoutes from "../../util/ApiRoutes";
import AlertsTable from "../alerts/AlertsTable";
import {Presets} from "../shared/timerange/TimeRange";
import BSSIDAndSSIDChart from "./bssids/BSSIDAndSSIDChart";
import DiscoHistogram from "./disco/DiscoHistogram";

const alertsService = new DetectionAlertsService();

export default function Dot11OverviewPage() {

  const user = useContext(UserContext);

  const [alerts, setAlerts] = useState(null);

  useEffect(() => {
    if (userHasPermission(user, "alerts_view")) {
      alertsService.findAllAlerts(setAlerts, 10, 0, "DOT11");
    }
  }, []);

  if (userHasPermission(user, "alerts_view") && !alerts) {
    return <LoadingSpinner />
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
          <div className="col-3">
            <NumberCard title="Active Alerts"
                        href={ApiRoutes.ALERTS.INDEX}
                        value={alerts.total_active}
                        numberFormat="0,0"
                        className={alerts.alerts.length > 0 ? "bg-danger" : null}/>
          </div>
        </WithPermission>

        <div className="col-3">
          <div className="card">
            <div className="card-body">
              <h3>Access Points (BSSIDs)</h3>

              <BSSIDAndSSIDChart parameter="bssid_count" timeRange={Presets.RELATIVE_HOURS_24}/>
            </div>
          </div>
        </div>

        <div className="col-3">
          <div className="card">
            <div className="card-body">
              <h3>Networks (SSIDs)</h3>

              <BSSIDAndSSIDChart parameter="ssid_count" timeRange={Presets.RELATIVE_HOURS_24}/>
            </div>
          </div>
        </div>

        <div className="col-3">
          <div className="card">
            <div className="card-body">
              <h3>Global Disconnections</h3>

              <DiscoHistogram discoType="disconnection" timeRange={Presets.RELATIVE_HOURS_24} />
            </div>
          </div>
        </div>

      </div>

      <WithPermission permission="alerts_view">
        <div className="row mt-3">
          <div className="col-12">
            <div className="card">
              <div className="card-body">
                <h3>All Alerts</h3>

                <AlertsTable perPage={5} hideControls={true}/>
              </div>
            </div>
          </div>
        </div>
      </WithPermission>

    </React.Fragment>
)

}
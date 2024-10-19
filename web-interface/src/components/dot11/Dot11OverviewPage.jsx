import React, {useContext, useEffect, useState} from "react";
import DetectionAlertsService from "../../services/DetectionAlertsService";
import LoadingSpinner from "../misc/LoadingSpinner";
import NumberCard from "../widgets/presentation/NumberCard";
import {userHasPermission} from "../../util/Tools";
import {UserContext} from "../../App";
import WithPermission from "../misc/WithPermission";

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

      <WithPermission permission="alerts_view">
        <div className="row mt-3">
          <div className="col-6">
            <NumberCard title="Active Alerts" value={alerts.total_active} numberFormat="0,0"/>
          </div>
        </div>
      </WithPermission>
    </React.Fragment>
  )

}
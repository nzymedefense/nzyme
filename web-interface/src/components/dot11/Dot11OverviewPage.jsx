import React, {useEffect, useState} from "react";
import DetectionAlertsService from "../../services/DetectionAlertsService";
import LoadingSpinner from "../misc/LoadingSpinner";
import NumberCard from "../widgets/presentation/NumberCard";

const alertsService = new DetectionAlertsService();

export default function Dot11OverviewPage() {

  const [alerts, setAlerts] = useState(null);

  useEffect(() => {
    alertsService.findAllAlerts(setAlerts, 10, 0, "DOT11");
  }, []);

  if (!alerts) {
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
        <div className="col-6">
          <NumberCard title="Active Alerts" value={alerts.total_active} numberFormat="0,0"/>
        </div>
      </div>
    </React.Fragment>
  )

}
import React, {useContext, useEffect, useState} from 'react'
import WithExactRole from "../misc/WithExactRole";
import CardTitleWithControls from "../shared/CardTitleWithControls";
import ApiRoutes from "../../util/ApiRoutes";
import HealthConsole from "../system/health/HealthConsole";
import SystemService from "../../services/SystemService";
import {TapContext, UserContext} from "../../App";
import DetectionAlertsService from "../../services/DetectionAlertsService";
import {userHasPermission, userHasSubsystem} from "../../util/Tools";
import WithPermission from "../misc/WithPermission";
import NumberCard from "../widgets/presentation/NumberCard";
import AlertsTable from "../alerts/AlertsTable";
import WithSubsystem from "../misc/WithSubsystem";
import BSSIDAndSSIDChart from "../dot11/bssids/BSSIDAndSSIDChart";
import {Presets} from "../shared/timerange/TimeRange";
import DiscoHistogram from "../dot11/disco/DiscoHistogram";
import {disableTapSelector, enableTapSelector} from "../misc/TapSelector";
import DNSService from "../../services/ethernet/DNSService";
import DNSStatisticsChart from "../ethernet/dns/DNSStatisticsChart";

const alertsService = new DetectionAlertsService();
const systemService = new SystemService();
const dnsService = new DNSService()

function byteConversion (x) {
  return x / 1024
}

export default function OverviewPage() {

  const tapContext = useContext(TapContext);
  const selectedTaps = tapContext.taps;

  const user = useContext(UserContext);

  const [alerts, setAlerts] = useState(null);
  const [indicators, setIndicators] = useState(null);

  const [dnsQueryStats, setDnsQueryStats] = useState(null);
  const [dnsResponseStats, setDnsResponseStats] = useState(null);
  const [dnsNxdomainStats, setDnsNxdomainStats] = useState(null);

  useEffect(() => {
    enableTapSelector(tapContext);

    return () => {
      disableTapSelector(tapContext);
    }
  }, [tapContext]);

  useEffect(() => {
    if (user.is_superadmin) {
      systemService.getHealthIndicators(setIndicators)
    }

    if (userHasPermission(user, "alerts_view")) {
      alertsService.findAllAlerts(setAlerts, 10, 0);
    }

    if (userHasSubsystem(user, "ethernet")) {
      dnsService.getGlobalChart(Presets.RELATIVE_HOURS_24, selectedTaps, "request_bytes", setDnsQueryStats);
      dnsService.getGlobalChart(Presets.RELATIVE_HOURS_24, selectedTaps, "response_bytes", setDnsResponseStats);
      dnsService.getGlobalChart(Presets.RELATIVE_HOURS_24, selectedTaps, "nxdomain_count", setDnsNxdomainStats);

    }
  }, [])

  const noContentInfo = () => {
    if (!user.is_superadmin && !userHasPermission(user, "alerts_view")
        && !userHasSubsystem(user, "ethernet") && !userHasSubsystem(user, "dot11")) {
      return (
          <div className="alert alert-info mt-3">
            Please enable at least one nzyme subsystem to display data on this dashboard.
          </div>
      )
    }
  }

  return (
      <React.Fragment>
        <div className="row">
          <div className="col-12">
            <h1>System Dashboard</h1>
          </div>
        </div>

        {noContentInfo()}

        <WithExactRole role="SUPERADMIN">
          <div className="row mt-3">
            <div className="col-12">
              <div className="card">
                <div className="card-body">
                  <CardTitleWithControls title="System Health Indicators"
                                         slim={true}
                                         internalLink={ApiRoutes.SYSTEM.HEALTH.INDEX}/>

                  <HealthConsole indicators={indicators}/>
                </div>
              </div>
            </div>
          </div>
        </WithExactRole>

        <WithPermission permission="alerts_view">
          <div className="row mt-3">
            <div className="col-3">
              <NumberCard title="Active Alerts (All Subsystems)"
                          internalLink={ApiRoutes.ALERTS.INDEX}
                          value={alerts ? alerts.total_active : 0}
                          numberFormat="0,0"
                          fullHeight={true}
                          className={(alerts ? (alerts.total_active > 0 ? "bg-danger" : null) : null)}/>
            </div>
          </div>
        </WithPermission>

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

        <WithSubsystem subsystem="dot11">
          <div className="row mt-5">
            <div className="col-12">
              <h2>802.11/WiFi Overview</h2>
            </div>
          </div>

          <div className="row mt-3">
            <div className="col-4">
              <div className="card">
                <div className="card-body">
                  <CardTitleWithControls title="Access Points"
                                         slim={true}
                                         internalLink={ApiRoutes.DOT11.NETWORKS.BSSIDS}/>

                  <BSSIDAndSSIDChart parameter="bssid_count" timeRange={Presets.RELATIVE_HOURS_24}/>
                </div>
              </div>
            </div>

            <div className="col-4">
              <div className="card">
                <div className="card-body">
                  <CardTitleWithControls title="Networks"
                                         slim={true}
                                         internalLink={ApiRoutes.DOT11.NETWORKS.BSSIDS}/>

                  <BSSIDAndSSIDChart parameter="ssid_count" timeRange={Presets.RELATIVE_HOURS_24}/>
                </div>
              </div>
            </div>

            <div className="col-4">
              <div className="card">
                <div className="card-body">
                  <CardTitleWithControls title="Disconnections"
                                         slim={true}
                                         internalLink={ApiRoutes.DOT11.DISCO.INDEX}/>

                  <DiscoHistogram discoType="disconnection" timeRange={Presets.RELATIVE_HOURS_24}/>
                </div>
              </div>
            </div>
          </div>
        </WithSubsystem>

        <WithSubsystem subsystem="ethernet">
          <div className="row mt-5">
            <div className="col-12">
              <h2>Ethernet Overview</h2>
            </div>
          </div>

          <div className="row mt-3">
            <div className="col-4">
              <div className="card">
                <div className="card-body">
                  <CardTitleWithControls title="DNS Query Traffic"
                                         slim={true}
                                         internalLink={ApiRoutes.ETHERNET.DNS.INDEX}/>

                  <DNSStatisticsChart data={dnsQueryStats}
                                      conversion={byteConversion}
                                      valueType="KB"/>
                </div>
              </div>
            </div>

            <div className="col-4">
              <div className="card">
                <div className="card-body">
                  <CardTitleWithControls title="DNS Response Traffic"
                                         slim={true}
                                         internalLink={ApiRoutes.ETHERNET.DNS.INDEX}/>

                  <DNSStatisticsChart data={dnsResponseStats}
                                      conversion={byteConversion}
                                      valueType="KB"/>
                </div>
              </div>
            </div>

            <div className="col-4">
              <div className="card">
                <div className="card-body">
                  <CardTitleWithControls title="DNS NXDOMAIN Responses"
                                         slim={true}
                                         internalLink={ApiRoutes.ETHERNET.DNS.INDEX}/>

                  <DNSStatisticsChart data={dnsNxdomainStats} attribute="nxdomain_count"/>
                </div>
              </div>
            </div>
          </div>
        </WithSubsystem>
      </React.Fragment>
  )

}
import React, {useContext, useEffect, useState} from 'react'
import DNSStatisticsChart from './DNSStatisticsChart'
import DNSNumbers from './DNSNumbers'
import {TapContext} from "../../../App";
import {disableTapSelector, enableTapSelector} from "../../misc/TapSelector";
import {Presets} from "../../shared/timerange/TimeRange";
import CardTitleWithControls from "../../shared/CardTitleWithControls";
import DNSService from "../../../services/ethernet/DNSService";
import DNSContactAttemptsTable from "./DNSContactAttemptsTable";

function byteConversion (x) {
  return x / 1024
}

const dnsService = new DNSService()

function DNSOverviewPage () {
  const [statistics, setStatistics] = useState(null);

  const tapContext = useContext(TapContext);
  const selectedTaps = tapContext.taps;

  const [timeRange, setTimeRange] = useState(Presets.RELATIVE_HOURS_24);
  const [contactAttemptsTimeRange, setContactAttemptsTimeRange] = useState(Presets.RELATIVE_HOURS_24);

  useEffect(() => {
    setStatistics(null);
    dnsService.findDNSStatistics(timeRange, selectedTaps, setStatistics);
  }, [selectedTaps, timeRange])

  useEffect(() => {
    enableTapSelector(tapContext);

    return () => {
      disableTapSelector(tapContext);
    }
  }, [tapContext]);

  return (
    <div>
      <div className="row">
        <div className="col-md-12">
          <div className="card">
            <div className="card-body">
              <CardTitleWithControls title="DNS Overview"
                                     helpLink="https://go.nzyme.org/ethernet-dns"
                                     slim={true}
                                     timeRange={timeRange}
                                     setTimeRange={setTimeRange} />
            </div>
          </div>
        </div>
      </div>

      <div className="row mt-3">
        <DNSNumbers fixedAppliedTimeRange={timeRange} data={statistics} />
      </div>

      <div className="row mt-3">
        <div className="col-md-6">
          <div className="card">
            <div className="card-body">
              <CardTitleWithControls title="Queries/Minute"
                                     slim={true}
                                     fixedAppliedTimeRange={timeRange} />

              <DNSStatisticsChart statistics={statistics} attribute="request_count" />
            </div>
          </div>
        </div>

        <div className="col-md-6">
          <div className="card">
            <div className="card-body">
              <CardTitleWithControls title="NXDOMAIN/Minute"
                                     slim={true}
                                     fixedAppliedTimeRange={timeRange} />

              <DNSStatisticsChart statistics={statistics} attribute="nxdomain_count" />
            </div>
          </div>
        </div>
      </div>

      <div className="row mt-3">
        <div className="col-md-6">
          <div className="card">
            <div className="card-body">
              <CardTitleWithControls title="Query Bytes/Minute"
                                     slim={true}
                                     fixedAppliedTimeRange={timeRange} />

              <DNSStatisticsChart statistics={statistics}
                                  attribute="request_bytes"
                                  conversion={byteConversion}
                                  valueType="KB" />
            </div>
          </div>
        </div>

        <div className="col-md-6">
            <div className="card">
              <div className="card-body">
                <CardTitleWithControls title="Response Bytes/Minute"
                                       slim={true}
                                       fixedAppliedTimeRange={timeRange} />

                <DNSStatisticsChart statistics={statistics}
                                    attribute="response_bytes"
                                    conversion={byteConversion}
                                    valueType="KB" />
              </div>
          </div>
        </div>
      </div>

      <div className="row mt-3">
        <div className="col-md-6">
          <div className="card">
            <div className="card-body">
              <CardTitleWithControls title="DNS Server Contact Attempts"
                                     timeRange={contactAttemptsTimeRange}
                                     setTimeRange={setContactAttemptsTimeRange} />

              <DNSContactAttemptsTable timeRange={contactAttemptsTimeRange} />
            </div>
          </div>
        </div>
      </div>

    </div>
  )
}

export default DNSOverviewPage

import React, {useContext, useEffect, useState} from 'react'
import DNSStatisticsChart from './DNSStatisticsChart'
import EthernetService from '../../../services/EthernetService'
import DNSNumbers from './DNSNumbers'
import DNSContactAttempsSummaryTable from './DNSContactAttempsSummaryTable'
import {TapContext} from "../../../App";
import {disableTapSelector, enableTapSelector} from "../../misc/TapSelector";

function byteConversion (x) {
  return x / 1024
}

const ethernetService = new EthernetService()

function DNSOverviewPage () {
  const [statistics, setStatistics] = useState(null)

  const tapContext = useContext(TapContext);

  const selectedTaps = tapContext.taps;

  useEffect(() => {
    setStatistics(null);
    ethernetService.findDNSStatistics(24, selectedTaps, setStatistics);
  }, [selectedTaps])

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
          <h1>DNS - Last 24 hours</h1>
        </div>
      </div>

      <div className="alert alert-danger">
        This is an early alpha page that should not be taken as an example of what future functionality will look like.
      </div>

      <div className="row mt-3">
        <DNSNumbers data={statistics} />
      </div>

      <div className="row mt-3">
        <div className="col-md-6">
          <div className="card">
            <div className="card-body">
              <h3>Queries/Minute</h3>
              <DNSStatisticsChart statistics={statistics} attribute="request_count" />
            </div>
          </div>
        </div>

        <div className="col-md-6">
          <div className="card">
            <div className="card-body">
              <h3>NXDOMAIN/Minute</h3>
              <DNSStatisticsChart statistics={statistics} attribute="nxdomain_count" />
            </div>
          </div>
        </div>
      </div>

      <div className="row mt-3">
        <div className="col-md-6">
          <div className="card">
            <div className="card-body">
              <h3>Query Bytes/Minute</h3>
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
                <h3>Response Bytes/Minute</h3>
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
              <h3>DNS Server Contact Attempts <small>(Top 10)</small></h3>
              <DNSContactAttempsSummaryTable data={statistics} />
            </div>
          </div>
        </div>
      </div>

      </div>
    )
  }

export default DNSOverviewPage

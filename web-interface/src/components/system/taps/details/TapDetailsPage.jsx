import React, { useEffect, useState } from 'react'
import TapsService from '../../../../services/TapsService'
import {useParams} from 'react-router-dom'
import LoadingSpinner from '../../../misc/LoadingSpinner'
import moment from 'moment'
import Routes from '../../../../util/ApiRoutes'
import numeral from 'numeral'
import byteAverageToMbit from '../../../../util/Tools'
import Buses from './Buses'
import TapInactiveWarning from './TapInactiveWarning'
import CaptureConfiguration from '../capture/CaptureConfiguration'
import TapMetrics from './metrics/TapMetrics'
import TapMetricsChartProxy from './metrics/TapMetricsChartProxy'
import TapClockWarning from "./TapClockWarning";
import TapDeletedWarning from "./TapDeletedWarning";

const tapsService = new TapsService()

function fetchData (uuid, setTap, setTapMetrics) {
  tapsService.findTap(uuid, setTap)
  tapsService.findMetricsOfTap(uuid, setTapMetrics)
}

function TapDetailsPage () {
  const { uuid } = useParams()

  const [tap, setTap] = useState(null)
  const [tapMetrics, setTapMetrics] = useState(null)

  useEffect(() => {
    fetchData(uuid, setTap, setTapMetrics)
    const id = setInterval(() => fetchData(uuid, setTap, setTapMetrics), 5000)
    return () => clearInterval(id)
  }, [uuid])

  if (!tap) {
    return <LoadingSpinner />
  }

  return (
    <div>
      <div className="row">

        <div className="col-md-10">
          <nav aria-label="breadcrumb">
            <ol className="breadcrumb">
              <li className="breadcrumb-item"><a href={Routes.SYSTEM.TAPS.INDEX}>Taps</a></li>
              <li className="breadcrumb-item active" aria-current="page">{tap.name}</li>
            </ol>
          </nav>
        </div>

        <div className="col-md-2">
          <a className="btn btn-primary float-end" href={Routes.SYSTEM.TAPS.INDEX}>Back</a>
        </div>
      </div>

      <div className="row">
        <div className="col-md-12">
          <h1>Tap &quot;{tap.name}&quot;</h1>
        </div>
      </div>

      <TapDeletedWarning tap={tap} />
      <TapInactiveWarning tap={tap} />
      <TapClockWarning tap={tap} />

      <div className="row mt-3">
        <div className="col-md-4">
          <div className="card">
            <div className="card-body">
              <h3>Throughput</h3>

              <dl>
                <dt>Throughput</dt>
                <dd>
                  {tap.active ? byteAverageToMbit(tap.processed_bytes.average) : "n/a" }{' '}
                  {tap.active ? "(" + numeral(tap.processed_bytes.average / 10).format('0 b') + "/sec)" : null}
                </dd>

                <dt>Total data processed since last restart</dt>
                <dd>{tap.active ? numeral(tap.processed_bytes.total).format('0.0 b') : "n/a"}</dd>
              </dl>
            </div>
          </div>
        </div>

        <div className="col-md-4">
          <div className="card">
            <div className="card-body">
              <h3>Metrics</h3>

              <dl>
                <dt>CPU Load</dt>
                <dd>{tap.active ? numeral(tap.cpu_load).format('0.0') + "%" : "n/a"}</dd>

                <dt>System-Wide Memory Usage</dt>
                <dd>
                  {tap.active ? numeral(tap.memory_used).format('0 b') + "/" + numeral(tap.memory_total).format('0 b') : "n/a"}{' '}
                  {tap.active ? "(" + numeral(tap.memory_used / tap.memory_total * 100).format('0.0') + "%)" : null}
                </dd>
              </dl>
            </div>
          </div>
        </div>

        <div className="col-md-4">
          <div className="card">
            <div className="card-body">
              <h3>Details</h3>

              <dl>
                <dt>Last Report</dt>
                <dd>
                  {tap.last_report ?
                      <span title={moment(tap.last_report).format()}> {moment(tap.last_report).fromNow()}</span>
                      : "no report yet" }
                </dd>

                <dt>Created at</dt>
                <dd>
                  <span title={moment(tap.created_at).format()}>{moment(tap.created_at).fromNow()}</span>
                </dd>

                <dt>Version</dt>
                <dd>{tap.version ? tap.version : "unknown"}</dd>
              </dl>
            </div>
          </div>
        </div>
      </div>

      <div className="row mt-3">
        <div className="col-md-12">
          <div className="card">
            <div className="card-body">
              <h3>Throughput</h3>

              <TapMetricsChartProxy type="gauge" name="system.captures.throughput_bit_sec" tapUuid={tap.uuid} />
            </div>
          </div>
        </div>
      </div>

      <div className="row mt-3">
        <div className="col-md-12">
          <div className="card">
            <div className="card-body">
              <h3>Capture Configuration</h3>

              <CaptureConfiguration tap={tap} />
            </div>
          </div>
        </div>
      </div>

      <div className="row mt-3">
        <div className="col-md-12">
          <div className="card">
            <div className="card-body">
              <h3>Buses &amp; Channels</h3>

              <Buses tap={tap} />
            </div>
          </div>
        </div>
      </div>

      <div className="row mt-3">
        <div className="col-md-6">
          <div className="card">
            <div className="card-body">
              <h3>Metrics</h3>

              <TapMetrics tap={tap} metrics={tapMetrics} />
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}

export default TapDetailsPage

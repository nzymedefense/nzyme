import React from 'react'
import TapMetricsGauges from './TapMetricsGauges'
import LoadingSpinner from '../../../../misc/LoadingSpinner'
import TapMetricsTimers from "./TapMetricsTimers";

function TapMetrics (props) {
  if (!props.metrics) {
    return <LoadingSpinner />
  }

  return (
        <div className="row mt-3">
          <div className="col-6">
            <div className="card">
              <div className="card-body">
                <h3>Metrics: Gauges</h3>
                <TapMetricsGauges tap={props.tap} gauges={props.metrics.gauges}/>
              </div>
            </div>
          </div>

          <div className="col-6">
            <div className="card">
              <div className="card-body">
                <h3>Metrics: Timers</h3>
                <TapMetricsTimers tap={props.tap} timers={props.metrics.timers} />
              </div>
            </div>
          </div>
        </div>
  )
}

export default TapMetrics
import React, { useEffect, useState } from 'react'
import MonitoringService from '../../../services/MonitoringService'
import ExportersTable from './ExportersTable'

const monitoringService = new MonitoringService()

function MonitoringPage () {
  const [summary, setSummary] = useState(null)

  useEffect(() => {
    monitoringService.getSummary(setSummary)
  }, [])

  return (
        <div>
            <div className="row">
                <div className="col-md-12">
                    <h1>Monitoring &amp; Metrics</h1>
                </div>
            </div>

            <div className="row">
                <div className="col-md-6">
                    <div className="card">
                        <div className="card-body">
                            <h3>Metrics Exporters</h3>

                            <p>
                                The nzyme system can make internal metrics available to other monitoring systems. Learn
                                more about metrics exporters in
                                the <a href="https://go.nzyme.org/metrics-exporters" target="_blank" rel="noreferrer">nzyme documentation</a>.
                            </p>

                            <ExportersTable summary={summary} />
                        </div>
                    </div>
                </div>
            </div>

        </div>
  )
}

export default MonitoringPage

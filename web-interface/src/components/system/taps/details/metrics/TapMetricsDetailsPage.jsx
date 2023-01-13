import React, { useEffect, useState } from 'react'
import { useParams } from 'react-router-dom'
import ApiRoutes from '../../../../../util/ApiRoutes'
import TapInactiveWarning from '../TapInactiveWarning'
import TapsService from '../../../../../services/TapsService'
import LoadingSpinner from '../../../../misc/LoadingSpinner'
import TapMetricsChartProxy from './TapMetricsChartProxy'

const tapsService = new TapsService()

function fetchData (tapName, setTap) {
  tapsService.findTap(tapName, setTap)
}

function TapMetricsDetailsPage () {
  const { tapName, metricType, metricName } = useParams()

  const [tap, setTap] = useState(null)

  useEffect(() => {
    fetchData(tapName, setTap)
    const id = setInterval(() => fetchData(tapName, setTap), 5000)
    return () => clearInterval(id)
  }, [tapName, setTap])

  if (!tap) {
    return <LoadingSpinner />
  }

  return (
        <div>
            <div className="row">
                <div className="col-md-10">
                    <nav aria-label="breadcrumb">
                        <ol className="breadcrumb">
                            <li className="breadcrumb-item"><a href={ApiRoutes.SYSTEM.TAPS.INDEX}>Taps</a></li>
                            <li className="breadcrumb-item" aria-current="page">
                                <a href={ApiRoutes.SYSTEM.TAPS.DETAILS(tap.name)}>{tap.name}</a>
                            </li>
                            <li className="breadcrumb-item" aria-current="page">Metrics</li>
                            <li className="breadcrumb-item active" aria-current="page">{metricName}</li>
                        </ol>
                    </nav>
                </div>
                <div className="col-md-2">
                    <a className="btn btn-primary float-end" href={ApiRoutes.SYSTEM.TAPS.DETAILS(tap.name)}>Back</a>
                </div>
            </div>

            <div className="row">
                <h1>Tap &quot;{tap.name}&quot; / Metric &quot;{metricName}&quot; <small>Type: {metricType}/average</small></h1>
            </div>

            <TapInactiveWarning tap={tap} />

            <div className="row mt-3">
                <div className="col-md-12">
                    <div className="card">
                        <div className="card-body">
                            <h3>Chart</h3>

                            <TapMetricsChartProxy type={metricType} name={metricName} tapName={tapName} />
                        </div>
                    </div>
                </div>
            </div>
        </div>
  )
}

export default TapMetricsDetailsPage

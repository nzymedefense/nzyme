import React, { useEffect, useState } from 'react'
import TapMetricsGaugeChart from './charts/TapMetricGaugeChart'
import TapsService from '../../../../../services/TapsService'

const tapsService = new TapsService()

function fetchData (tapName, metricName, setData) {
  tapsService.findGaugeMetricHistogramOfTap(tapName, metricName, setData)
}

function throughputConversion (x) {
  return x / 1024 / 1024
}

function byteConversion (x) {
  return x / 1024 / 1024
}

function TapMetricsChartProxy (props) {
  const [data, setData] = useState(null)

  const tapUuid = props.tapUuid;
  const name = props.name;

  useEffect(() => {
    fetchData(tapUuid, name, setData)
    const id = setInterval(() => fetchData(tapUuid, name, setData), 30000)
    return () => clearInterval(id)
  }, [tapUuid, name])

  if (props.type === 'gauge') {
    let conversion
    let valueType

    if (props.name.includes('bit') && props.name.includes('sec')) {
      conversion = throughputConversion
      valueType = 'Mbit/sec'
    }

    if (props.name.includes('byte')) {
      conversion = byteConversion
      valueType = 'MB'
    }

    if (props.name.includes('percent')) {
      valueType = '%'
    }

    return (
      <TapMetricsGaugeChart data={data} conversion={conversion} valueType={valueType} />
    )
  } else {
    return <div className="alert alert-danger">Unknown metric type.</div>
  }
}

export default TapMetricsChartProxy

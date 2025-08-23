import React, { useEffect, useState } from 'react'
import TapsService from '../../../../../services/TapsService'
import TapMetricsChart from "./charts/TapMetricChart";

const tapsService = new TapsService()

function fetchGaugeData (tapName, metricName, setData) {
  tapsService.findGaugeMetricHistogramOfTap(tapName, metricName, setData)
}
function fetchTimerData (tapName, metricName, setData) {
  tapsService.findTimerMetricHistogramOfTap(tapName, metricName, setData)
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
    if (props.type === "gauge") {
      fetchGaugeData(tapUuid, name, setData)
    }

    if (props.type === "timer") {
      fetchTimerData(tapUuid, name, setData)
    }
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

    if (props.name.includes('temperature')) {
      valueType = '&deg;C'
    }

    return (
        <TapMetricsChart data={data} conversion={conversion} valueType={valueType}/>
    )
  } else if (props.type === 'timer') {
    return <TapMetricsChart data={data} valueType="&#956;s" />
  } else {
    return <div className="alert alert-danger">Unknown metric type.</div>
  }
}

export default TapMetricsChartProxy

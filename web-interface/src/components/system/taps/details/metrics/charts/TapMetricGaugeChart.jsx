import React from 'react'
import SimpleLineChart from '../../../../../widgets/charts/SimpleLineChart'
import LoadingSpinner from '../../../../../misc/LoadingSpinner'

function TapMetricsGaugeChart (props) {
  if (!props.data) {
    return <LoadingSpinner />
  }

  if (!props.data.values) {
    return <div className="alert alert-warning">No recent data.</div>
  }

  return <SimpleLineChart
        height={200}
        data={formatData(props.data.values, props.conversion)}
        customMarginLeft={85}
        customMarginRight={25}
        ticksuffix={props.valueType ? ' ' + props.valueType : undefined}
        tickformat={'.2~f'}
    />
}

function formatData (data, conversion) {
  const result = {}

  Object.keys(data).sort().forEach(function (key) {
    if (conversion) {
      result[key] = conversion(data[key].average)
    } else {
      result[key] = data[key].average
    }
  })

  return result
}

export default TapMetricsGaugeChart

import React from 'react'
import SimpleBarChart from '../../widgets/charts/SimpleBarChart'
import GenericWidgetLoadingSpinner from "../../widgets/GenericWidgetLoadingSpinner";

function DNSStatisticsChart (props) {
  if (!props.statistics) {
    return <GenericWidgetLoadingSpinner height={150} />
  }

  return <SimpleBarChart
        height={150}
        lineWidth={1}
        customMarginLeft={45}
        data={formatData(props.statistics.buckets, props.attribute, props.conversion)}
        ticksuffix={props.valueType ? ' ' + props.valueType : undefined}
        tickformat={'.2~f'}
    />
}

function formatData (data, attribute, conversion) {
  const result = {}

  Object.keys(data).sort().forEach(function (key) {
    if (conversion) {
      result[key] = conversion(data[key][attribute])
    } else {
      result[key] = data[key][attribute]
    }
  })

  return result
}

export default DNSStatisticsChart

import React, {useEffect, useState} from "react";
import ClusterService from "../../../../../services/ClusterService";
import LoadingSpinner from "../../../../misc/LoadingSpinner";
import SimpleLineChart from "../../../../widgets/charts/SimpleLineChart";

const clusterService = new ClusterService()

function fetchData(nodeId, metricName, setHistogram) {
  clusterService.findGaugeMetricHistogramOfNode(nodeId, metricName, setHistogram)
}

function formatData(data) {
  const result = {}

  Object.keys(data).sort().forEach(function (key) {
    result[key] = data[key].sum / 1024 / 1024
  })

  return result
}

function TapReportStatisticsChart(props) {

  const nodeId = props.nodeId;
  const metricName = "tap_report_size";
  const [histogram, setHistogram] = useState(null)

  useEffect(() => {
    fetchData(nodeId, metricName, setHistogram)
    const id = setInterval(() => fetchData(nodeId, metricName, setHistogram), 30000)
    return () => clearInterval(id)
  }, [nodeId, setHistogram])

  if (!histogram) {
    return <LoadingSpinner />
  }

  return <SimpleLineChart
      height={200}
      data={formatData(histogram.values)}
      customMarginLeft={85}
      customMarginRight={25}
      ticksuffix="MB"
      tickformat={'.2~f'}
  />

}

export default TapReportStatisticsChart;

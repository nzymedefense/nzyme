import React from "react";
import GaugeRow from "../../../../misc/metrics/GaugeRow";

function NodeGauges(props) {

  const gauges = props.gauges;
  
  return (
      <table className="table table-sm table-hover table-striped">
        <thead>
        <tr>
          <th>Metric</th>
          <th>Value</th>
        </tr>
        </thead>
        <tbody>
          <GaugeRow title="GeoIP Cache Size" numberFormat="0,0" gauge={gauges.geoip_cache_size} />
        </tbody>
      </table>
  )

}

export default NodeGauges;
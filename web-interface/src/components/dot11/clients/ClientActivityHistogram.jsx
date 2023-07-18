import React, {useState} from "react";
import SimpleLineChart from "../../charts/SimpleLineChart";

function ClientActivityHistogram(props) {

  const histogram = props.histogram;
  const parameter = props.parameter;

  const formatData = function(data) {
    const result = {}

    Object.keys(data).sort().forEach(function(key) {
      result[key] = data[key][parameter];
    })

    return result
  }

  return (
      <React.Fragment>


        <SimpleLineChart
            height={200}
            lineWidth={1}
            customMarginBottom={35}
            customMarginRight={20}
            data={formatData(histogram)} />
      </React.Fragment>
  )

}

export default ClientActivityHistogram;
import React from "react";
import Plot from 'react-plotly.js'
import Store from "../../../util/Store";

function SimpleRangeIndicator(props) {

  const suffix = props.suffix ? props.suffix : undefined
  const valueformat = props.valueformat ? props.valueformat : undefined
  const tickformat = props.tickformat ? props.tickformat : undefined

  const colors = {}
  if (Store.get('dark_mode')) {
    colors.background = '#262626'
    colors.text = '#f9f9f9'
    colors.bar = '#373737';
  } else {
    colors.background = '#f9f9f9'
    colors.text = '#111111'
    colors.bar = '#1d30d7'
  }

  const data = [
    {
      domain: { x: [0, 1], y: [0, 1] },
      value: props.value,
      type: "indicator",
      mode: "gauge+number",
      number: { suffix: suffix, font: { size: 34 }, valueformat: valueformat },
      gauge: { axis: { range: props.range, ticksuffix: suffix, tickformat: tickformat }, bar: {color: colors.bar} }
    }
  ]

  return (
      <Plot
          style={{ width: "100%", height: "225px" }}
          data={data}
          layout={{
            margin: { t: 0, b: 0, l: 40, r: 40 },
            font: {
              family: "'Nunito Sans', sans-serif",
              size: 12,
              color: colors.text
            },
            paper_bgcolor: colors.background,
            plot_bgcolor: colors.background
          }}
          config={{
            displayModeBar: false,
            autosize: true
          }}
          useResizeHandler
      />
  )

}

export default SimpleRangeIndicator;

import React from 'react'

import Plot from 'react-plotly.js'
import Store from "../../util/Store";

class SimpleLineChart extends React.Component {
  constructor (props) {
    super(props)

    this.state = {
      data: props.data
    }
  }

  componentWillReceiveProps (nextProps) {
    this.setState({ data: nextProps.data })
  }

  render () {
    const x = []
    const y = []

    const data = this.state.data

    let finalData = this.props.finalData
    if (!finalData) {
      Object.keys(data).forEach(function (key) {
        x.push(new Date(key))
        y.push(data[key])
      })

      finalData = [
        {
          x: x,
          y: y,
          type: 'scatter',
          line: { width: this.props.lineWidth ? this.props.lineWidth : 2, shape: 'linear', color: '#0d6efd' }
        }
      ]
    }

    const marginLeft = this.props.customMarginLeft ? this.props.customMarginLeft : 25
    const marginRight = this.props.customMarginRight ? this.props.customMarginRight : 0
    const marginTop = this.props.customMarginTop ? this.props.customMarginTop : 25
    const marginBottom = this.props.customMarginBottom ? this.props.customMarginBottom : 50

    let colors = {};
    if (Store.get("dark_mode")) {
        colors.background = "#2B2D42";
        colors.text = "#ffffff";
        colors.lines = "#8D99AE";
        colors.grid = "#8D99AE";
    } else {
        colors.background = "#ffffff";
        colors.text = "#212529";
        colors.lines = "#212529";
        colors.grid = "#e3e3e3";
    }

    return (
        <Plot
            style={{ width: '100%', height: '100%' }}
            data={finalData}
            layout={{
              height: this.props.height,
              width: this.props.width,
              font: {
                family: "'Nunito Sans', sans-serif",
                size: 12,
                color: colors.text
              },
              margin: { l: marginLeft, r: marginRight, b: marginBottom, t: marginTop, pad: 0 },
              title: { text: this.props.title },
              paper_bgcolor: colors.background,
              plot_bgcolor: colors.background,
              showlegend: false,
              dragmode: false,
              clickmode: 'none',
              hovermode: this.props.disableHover ? false : 'x',
              hoverlabel: {
                font: { size: 11 },
                namelength: -1
              },
              barmode: 'stack',
              boxgap: 0,
              xaxis: {
                fixedrange: true,
                title: this.props.xaxistitle,
                linecolor: colors.lines,
                linewidth: 1,
                gridcolor: colors.grid,
                zeroline: false
              },
              yaxis: {
                ticksuffix: this.props.ticksuffix ? this.props.ticksuffix : undefined,
                tickformat: this.props.tickformat ? this.props.tickformat : undefined,
                fixedrange: true,
                title: this.props.yaxistitle,
                linecolor: colors.lines,
                linewidth: 1,
                gridcolor: colors.grid,
                zeroline: false
              },
              annotations: this.props.annotations ? this.props.annotations : [],
              shapes: this.props.shapes
            }}
            config={{
              displayModeBar: false,
              autosize: true,
              responsive: true
            }}
        />
    )
  }
}

export default SimpleLineChart

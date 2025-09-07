import React from 'react'
import Plot from 'react-plotly.js'
import Store from '../../../util/Store'
import { Absolute } from "../../shared/timerange/TimeRange";

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
          mode: this.props.scattermode ? this.props.scattermode : 'lines',
          marker: { size: 3 },
          line: {
            width: this.props.lineWidth ? this.props.lineWidth : 2,
            shape: 'linear',
            color: Store.get('dark_mode') ? '#f9f9f9' : '#1d30d7'
          }
        }
      ]
    }

    let xRange = undefined
    if (x.length > 1) {
      const min = new Date(Math.min(...x.map(d => d.getTime())))
      const max = new Date(Math.max(...x.map(d => d.getTime())))
      xRange = [min, max]
    }

    const marginLeft = this.props.customMarginLeft ? this.props.customMarginLeft : 25
    const marginRight = this.props.customMarginRight ? this.props.customMarginRight : 0
    const marginTop = this.props.customMarginTop ? this.props.customMarginTop : 25
    const marginBottom = this.props.customMarginBottom ? this.props.customMarginBottom : 50

    const colors = {}
    if (Store.get('dark_mode')) {
      colors.background = '#262626'
      colors.text = '#f9f9f9'
      colors.lines = '#373737'
      colors.grid = '#373737'
    } else {
      colors.background = '#f9f9f9'
      colors.text = '#111111'
      colors.lines = '#373737'
      colors.grid = '#e6e6e6'
    }

    const interactive = Boolean(this.props.setTimeRange)

    const hovermode = this.props.disableHover
        ? false
        : (this.props.hovermode ?? (interactive ? "x" : "closest"))

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
              dragmode: interactive ? 'zoom' : false,
              clickmode: 'none',
              hovermode: hovermode,
              hoverlabel: {
                font: { size: 11 },
                namelength: -1
              },
              barmode: 'stack',
              boxgap: 0,
              xaxis: {
                fixedrange: !interactive,
                rangeslider: { visible: false },
                title: this.props.xaxistitle,
                linecolor: colors.lines,
                linewidth: 1,
                gridcolor: colors.grid,
                zeroline: false,
                range: xRange
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
              showAxisDragHandles: false,
              displayModeBar: false,
              autosize: true,
              responsive: true,
              showTips: false,
              scrollZoom: interactive
            }}
            onRelayout={event => {
              if (this.props.setTimeRange) {
                const x0 = event['xaxis.range[0]']
                const x1 = event['xaxis.range[1]']
                if (x0 != null && x1 != null) {
                  this.props.setTimeRange(Absolute(new Date(x0), new Date(x1)))
                }
              }}
            }
        />
    )
  }
}

export default SimpleLineChart

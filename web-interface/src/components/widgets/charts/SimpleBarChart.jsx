import React from 'react'

import Plot from 'react-plotly.js'
import Store from '../../../util/Store'
import {Absolute} from "../../shared/timerange/TimeRange";

class SimpleBarChart extends React.Component {
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
          type: 'bar',
          marker: { color: Store.get('dark_mode') ? '#e6e6e6' : '#1d30d7' }
        }
      ]
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

    let hovermode;
    if (this.props.disableHover) {
      hovermode = false;
    } else {
      if (this.props.hovermode) {
        hovermode = this.props.hovermode;
      } else {
        hovermode = "x";
      }
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
              dragmode: this.props.setTimeRange ? 'zoom' : false,
              clickmode: 'none',
              hovermode:  hovermode,
              hoverlabel: {
                font: { size: 11 },
                namelength: -1
              },
              barmode: 'stack',
              boxgap: 0,
              xaxis: {
                fixedrange: false,
                rangeslider: { visible: false },
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

export default SimpleBarChart

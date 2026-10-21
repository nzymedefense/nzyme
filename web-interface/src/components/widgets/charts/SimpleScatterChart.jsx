import React from 'react'
import Plot from 'react-plotly.js'
import Store from '../../../util/Store'
import { Absolute } from "../../shared/timerange/TimeRange";
import { useApplyTimeRange } from "../../shared/timerange/TimeRangeUrl";

class SimpleScatterChartBase extends React.Component {

  render () {
    const dark = Store.get('dark_mode')
    const palette = dark
      ? ['#e6e6e6', '#f5a623', '#4fc3f7', '#81c784', '#ce93d8', '#ff8a65', '#fff176', '#80cbc4']
      : ['#1d30d7', '#d9700a', '#0288d1', '#2e7d32', '#7b1fa2', '#d84315', '#9e9d24', '#00796b']

    const finalData = (this.props.series || []).map((s, i) => {
      return {
        name: s.name,
        x: s.x,
        y: s.y,
        text: s.text,
        hovertemplate: s.hovertemplate,
        type: 'scatter',
        mode: 'markers',
        marker: { color: palette[i % palette.length], size: 6, opacity: 0.8 }
      }
    })

    let xRange = undefined
    const timeRange = this.props.timeRange
    if (timeRange && timeRange.type === 'relative') {
      const now = new Date()
      xRange = [new Date(now.getTime() - timeRange.minutes * 60 * 1000), now]
    } else if (timeRange && timeRange.type === 'absolute') {
      xRange = [new Date(timeRange.from), new Date(timeRange.to)]
    }

    const colors = {}
    if (dark) {
      colors.background = '#1c1c22'
      colors.text = '#c4c4d4'
      colors.lines = '#33333d'
      colors.grid = '#2a2a33'
    } else {
      colors.background = '#f9f9f9'
      colors.text = '#111111'
      colors.lines = '#373737'
      colors.grid = '#e6e6e6'
    }

    const interactive = Boolean(this.props.setTimeRange)

    return (
      <Plot
        style={{ width: '100%', height: '100%' }}
        data={finalData}
        layout={{
          height: this.props.height,
          font: {
            family: "'Nunito Sans', sans-serif",
            size: 12,
            color: colors.text
          },
          margin: { l: this.props.customMarginLeft ? this.props.customMarginLeft : 60, r: 0, b: 50, t: 25, pad: 0 },
          paper_bgcolor: colors.background,
          plot_bgcolor: colors.background,
          showlegend: finalData.length > 1,
          legend: { orientation: 'h', y: 1.15 },
          dragmode: interactive ? 'zoom' : false,
          hovermode: 'closest',
          hoverlabel: {
            font: { size: 11 },
            namelength: -1
          },
          xaxis: {
            fixedrange: !interactive,
            rangeslider: { visible: false },
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
            rangemode: 'tozero',
            title: this.props.yaxistitle,
            linecolor: colors.lines,
            linewidth: 1,
            gridcolor: colors.grid,
            zeroline: false
          }
        }}
        config={{
          showAxisDragHandles: false,
          displayModeBar: false,
          autosize: true,
          responsive: true,
          showTips: false,
          scrollZoom: false
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

function SyncingScatterChart (props) {
  const applyTimeRange = useApplyTimeRange(
    props.setTimeRange,
    props.urlKey || "timerange",
    props.doNotPersistTimeRange || false
  )

  return <SimpleScatterChartBase {...props} setTimeRange={applyTimeRange} />
}

function SimpleScatterChart (props) {
  if (props.setTimeRange) {
    return <SyncingScatterChart {...props} />
  }

  return <SimpleScatterChartBase {...props} />
}

export default SimpleScatterChart

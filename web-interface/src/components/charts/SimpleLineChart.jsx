import React from 'react'

import Plot from 'react-plotly.js'

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
          line: { width: 2, shape: 'linear', color: '#0d6efd' }
        }
      ]
    }

    const marginLeft = this.props.customMarginLeft ? this.props.customMarginLeft : 25
    const marginRight = this.props.customMarginRight ? this.props.customMarginRight : 0
    const marginTop = this.props.customMarginTop ? this.props.customMarginTop : 25
    const marginBottom = this.props.customMarginBottom ? this.props.customMarginBottom : 50

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
                color: this.props.textColor ? this.props.textColor : '#212529'
              },
              margin: { l: marginLeft, r: marginRight, b: marginBottom, t: marginTop, pad: 0 },
              title: { text: this.props.title },
              paper_bgcolor: this.props.backgroundColor ? this.props.backgroundColor : '#ffffff',
              plot_bgcolor: this.props.backgroundColor ? this.props.backgroundColor : '#ffffff',
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
                linecolor: '#212529',
                linewidth: 1,
                gridcolor: '#e3e3e3',
                zeroline: false
              },
              yaxis: {
                ticksuffix: this.props.ticksuffix ? this.props.ticksuffix : undefined,
                tickformat: this.props.tickformat ? this.props.tickformat : undefined,
                fixedrange: true,
                title: this.props.yaxistitle,
                linecolor: '#212529',
                linewidth: 1,
                gridcolor: '#e3e3e3',
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

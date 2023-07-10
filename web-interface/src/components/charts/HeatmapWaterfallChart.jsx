import React from 'react'

import Plot from 'react-plotly.js'
import Store from "../../util/Store";

class HeatmapWaterfallChart extends React.Component {
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
    const data = this.state.data

    const colors = {}
    if (Store.get('dark_mode')) {
      colors.background = '#2B2D42'
      colors.text = '#ffffff'
      colors.lines = '#8D99AE'
      colors.grid = '#8D99AE'
      colors.scale = [
        [0, 'rgb(43, 45, 66)'], [0.35, 'rgb(13, 110, 253)'],
        [0.5, 'rgb(190,190,190)'], [0.6, 'rgb(220,170,132)'],
        [0.7, 'rgb(230,145,90)'], [1, 'rgb(178,10,28)']
      ]
    } else {
      colors.background = '#ffffff'
      colors.text = '#212529'
      colors.lines = '#212529'
      colors.grid = '#e3e3e3'
      colors.scale =  [
        [0, 'rgb(255,255,255)'], [0.35, 'rgb(13, 110, 253)'],
        [0.5, 'rgb(190,190,190)'], [0.6, 'rgb(220,170,132)'],
        [0.7, 'rgb(230,145,90)'], [1, 'rgb(178,10,28)']
      ]
    }

    const finalData = [
      {
        z: data.z,
        x: data.x,
        y: data.y,
        type: 'heatmap',
        hovertemplate: this.props.hovertemplate,
        showscale: false,
        colorscale: colors.scale,
      }
    ]

    const marginLeft = this.props.customMarginLeft ? this.props.customMarginLeft : 60
    const marginRight = this.props.customMarginRight ? this.props.customMarginRight : 60
    const marginTop = this.props.customMarginTop ? this.props.customMarginTop : 25
    const marginBottom = this.props.customMarginBottom ? this.props.customMarginBottom : 50

    return (
            <Plot
                style={{ width: '100%', height: this.props.height }}
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
                  plot_bgcolor: colors.lines,
                  showlegend: false,
                  dragmode: false,
                  clickmode: 'none',
                  hovermode: this.props.disableHover ? false : 'x',
                  xaxis: { visible: true, title: this.props.xaxistitle },
                  yaxis: { visible: true, title: this.props.yaxistitle },
                  shapes: this.props.layers ? this.props.layers.shapes : null,
                  annotations: this.props.annotations ? this.props.annotations : []
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

export default HeatmapWaterfallChart

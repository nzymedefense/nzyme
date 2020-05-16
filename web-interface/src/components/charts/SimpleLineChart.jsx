import React from 'react';

import Plot from 'react-plotly.js';

class SimpleLineChart extends React.Component {

  constructor(props) {
    super(props);

    this.state = {
        data: props.data
    };
  }

  componentWillReceiveProps(nextProps) {
    this.setState({ data: nextProps.data });
  }

  render() {
    let x = [];
    let y = [];

    const data = this.state.data;

    let finalData = this.props.finalData;
    if (!finalData) {
        Object.keys(data).forEach(function (key) {
            x.push(new Date(key));
            y.push(data[key]);
        });

        finalData = [
            {
                x: x,
                y: y,
                type: "scatter",
                line: {width: 1, shape: "linear", color: "#2983fe"},
            }
        ];
    }

    let marginLeft = this.props.customMarginLeft ? this.props.customMarginLeft : 25;
    let marginRight = this.props.customMarginRight ? this.props.customMarginRight : 0;
    let marginTop = this.props.customMarginTop ? this.props.customMarginTop : 25;
    let marginBottom = this.props.customMarginBottom ? this.props.customMarginBottom : 50;


      return (
        <Plot
            style={{ width: '100%', height: '100%' }}
            data={finalData}
            layout={{
                height: this.props.height,
                font: {
                    family: "'Inconsolata', monospace",
                    size: 10,
                    color: this.props.textColor ? this.props.textColor : "#ffffff"
                },
                margin: { l: marginLeft, r: marginRight, b: marginBottom, t: marginTop, pad: 0 },
                title: { text: this.props.title },
                paper_bgcolor: this.props.backgroundColor ? this.props.backgroundColor : "#0c0d16",
                plot_bgcolor: this.props.backgroundColor ? this.props.backgroundColor : "#0c0d16",
                showlegend: false,
                dragmode: false,
                clickmode: "none",
                hovermode: this.props.disableHover ? false : "x",
                hoverlabel: {
                    font: { size: 11 },
                    namelength: -1
                },
                barmode: "stack",
                boxgap: 0,
                xaxis: { fixedrange: true, title: this.props.xaxistitle },
                yaxis: { fixedrange: true, title: this.props.yaxistitle },
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

export default SimpleLineChart;
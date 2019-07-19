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
                  line: {width: 1, shape: "linear", color: "#2983fe"}
              }
          ];
      }

      let marginLeft = this.props.customMarginLeft ? this.props.customMarginLeft : 25;
      let marginRight = this.props.customMarginRight ? this.props.customMarginRight : 0;

    return (
      <Plot
        data={finalData}
        layout={{
          width: this.props.width,
          height: this.props.height,
          font: { family: "'Inconsolata', monospace", size: 10 },
          margin: { l: marginLeft, r: marginRight, b: 50, t: 25, pad: 0 },
          title: { text: this.props.title },
          paper_bgcolor: "#0c0d16",
          plot_bgcolor: "#0c0d16",
          showlegend: false,
          dragmode: false,
          clickmode: "none",
          hovermode: "x",
          hoverlabel: {
            font: { size: 11 },
            namelength: -1
          },
          connectgaps: true,
          xaxis: { fixedrange: true },
          yaxis: { fixedrange: true }
        }}
        config={{
          displayModeBar: false
        }}
      />
    )
  }

}

export default SimpleLineChart;
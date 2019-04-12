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

    Object.keys(data).forEach(function(key) {
      x.push(new Date(key));
      y.push(data[key]);
    });

    return (
      <Plot
        data={[
          {
            x: x,
            y: y,
            type: "scatter",
            line: { width: 1, shape: "linear", color: "#2983fe" }
          }
        ]}
        layout={{
          width: 335,
          height: 150,
          font: { family: "'Inconsolata', monospace", size: 10 },
          margin: { l: 25, r: 0, b: 50, t: 25, pad: 0 },
          title: { text: this.props.title },
          paper_bgcolor: "#0c0d16",
          plot_bgcolor: "#0c0d16",
          showlegend: false,
          dragmode: false,
          clickmode: "none",
          hovermode: "y",
          hoverlabel: {
            font: { size: 11 }
          },
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
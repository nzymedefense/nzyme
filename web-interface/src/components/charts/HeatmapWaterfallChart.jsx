import React from 'react';

import Plot from 'react-plotly.js';

class HeatmapWaterfallChart extends React.Component {

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
        const data = this.state.data;

        const finalData = [
            {
                z: data.z,
                x: data.x,
                y: data.y,
                type: "heatmap",
                hovertemplate: this.props.hovertemplate,
                showscale: false,
                colorscale: [
                    [0, 'rgb(12,13,22)'], [0.125, 'rgb(0,60,170)'],
                    [0.375, 'rgb(5,255,255)'], [0.625, 'rgb(255,255,0)'],
                    [0.875, 'rgb(250,0,0)'], [1, 'rgb(128,0,0)']
                ]
            }
        ];

        return (
            <Plot
                data={finalData}
                layout={{
                    width: 1100,
                    height: 400,
                    font: { family: "'Inconsolata', monospace", size: 10 },
                    margin: { l: 60, r: 60, b: 50, t: 25, pad: 0 },
                    title: { text: this.props.title },
                    paper_bgcolor: "#0c0d16",
                    plot_bgcolor: "#0c0d16",
                    showlegend: false,
                    dragmode: false,
                    clickmode: "none",
                    xaxis: { visible: true, title: this.props.xaxistitle },
                    yaxis: { visible: true, title: this.props.yaxistitle },
                    shapes: this.props.shapes
                }}
                config={{
                    displayModeBar: false
                }}
            />
        );
    }

}

export default HeatmapWaterfallChart;
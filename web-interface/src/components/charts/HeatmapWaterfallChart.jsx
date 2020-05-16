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
                    [0, 'rgb(12,13,22)'], [0.125, 'rgb(0,109,44)'],
                    [0.25, 'rgb(35,139,69)'], [0.375, 'rgb(65,171,93)'],
                    [0.5, 'rgb(116,196,118)'], [0.625, 'rgb(161,217,155)'],
                    [0.75, 'rgb(199,233,192)'], [0.875, 'rgb(229,245,224)'],
                    [1, 'rgb(247,252,245)']
                ]
            }
        ];

        return (
            <Plot
                style={{ width: '100%', height: '100%' }}
                data={finalData}
                layout={{
                    height: 400,
                    font: {
                        family: "'Inconsolata', monospace",
                        size: 10,
                        color: this.props.textColor ? this.props.textColor : "#ffffff"
                    },
                    margin: { l: 60, r: 60, b: 50, t: 25, pad: 0 },
                    title: { text: this.props.title },
                    paper_bgcolor: "#0c0d16",
                    plot_bgcolor: "#0c0d16",
                    showlegend: false,
                    dragmode: false,
                    clickmode: "none",
                    xaxis: { visible: true, title: this.props.xaxistitle },
                    yaxis: { visible: true, title: this.props.yaxistitle },
                    shapes: this.props.layers.shapes,
                    annotations: this.props.layers.annotations
                }}
                config={{
                    displayModeBar: false,
                    autosize: true,
                    responsive: true
                }}
            />
        );
    }

}

export default HeatmapWaterfallChart;
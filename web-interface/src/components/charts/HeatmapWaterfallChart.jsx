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

    hexToRgb(hex) {
        var result = /^#?([a-f\d]{2})([a-f\d]{2})([a-f\d]{2})$/i.exec(hex);
        return result ? {
            r: parseInt(result[1], 16),
            g: parseInt(result[2], 16),
            b: parseInt(result[3], 16)
        } : null;
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
                    [0, this.hexToRgb(this.props.backgroundColor ? this.props.backgroundColor : "#0c0d16")], [0.125, 'rgb(0,109,44)'],
                    [0.25, 'rgb(35,139,69)'], [0.375, 'rgb(65,171,93)'],
                    [0.5, 'rgb(116,196,118)'], [0.625, 'rgb(161,217,155)'],
                    [0.75, 'rgb(199,233,192)'], [0.875, 'rgb(229,245,224)'],
                    [1, 'rgb(247,252,245)']
                ]
            }
        ];

        let marginLeft = this.props.customMarginLeft ? this.props.customMarginLeft : 60;
        let marginRight = this.props.customMarginRight ? this.props.customMarginRight : 60;
        let marginTop = this.props.customMarginTop ? this.props.customMarginTop : 25;
        let marginBottom = this.props.customMarginBottom ? this.props.customMarginBottom : 50;

        return (
            <Plot
                style={{ width: '100%' }}
                data={finalData}
                layout={{
                    height: this.props.height,
                    width: this.props.width,
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
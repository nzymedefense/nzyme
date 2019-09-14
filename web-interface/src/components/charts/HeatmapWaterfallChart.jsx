import React from 'react';

import Plot from 'react-plotly.js';

class HeatmapWaterfallChart extends React.Component {

    render() {
        const finalData =  [
            {
                z: [
                    [0,0,0,0,0,0,100,0,0,0,0,0,0],
                    [0,0,0,0,0,0,105,0,0,0,0,0,0],
                    [0,0,0,0,0,50,90 ,0,0,0,0,0,0],
                    [0,0,0,0,0,0,120,0,0,0,0,0,0],
                    [0,0,0,0,0,0,95 ,0,0,0,0,0,0],
                ],
                x: [
                    -6,
                    -5,
                    -4,
                    -3,
                    -2,
                    -1,
                    0,
                    1,
                    2,
                    3,
                    4,
                    5,
                    6
                ],
                type: 'heatmap',

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
                    title: { text: "ONE PING ONLY" },
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
                    xaxis: { visible: true },
                    yaxis: { visible: false }
                }}
                config={{
                    displayModeBar: false
                }}
            />
        );
    }

}

export default HeatmapWaterfallChart;